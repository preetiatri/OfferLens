"""
Generates the public OfferLens web catalogue as static HTML.

Why static rather than a JS app that queries Firestore in the browser:

  * Crawlability. Search engines index server-delivered HTML reliably; JS-rendered
    content is indexed late and inconsistently. Organic search is the point of this
    site, so the HTML has to be in the response body.
  * Cost. A JS page would spend Firestore reads per visitor, undoing the catalogue
    sync work. Static files cost nothing to serve and cannot exhaust a quota.
  * Speed. Core Web Vitals feed ranking, and a static file on Apache is as fast as
    the web gets.

Data source is the published catalogue (catalogue/meta + catalogue/chunk_N) via the
Firestore REST API. Those documents are world-readable by design, so this script needs
no service-account key and no credentials of any kind - deliberately, since a key that
does not exist cannot leak. The raw `offers` collection is NOT readable anonymously
(verified 403), so the catalogue is both the cheapest and the only public source.

Usage:
    python build_web_catalogue.py            # writes ./web_out
    python build_web_catalogue.py --out DIR

Then upload the contents of web_out/ to /var/www/offerlens/ on the VPS.
Re-run after any admin-panel publish to refresh the site.
"""

import argparse
import html
import json
import os
import re
import sys
import urllib.request
from datetime import datetime, timezone

PROJECT_ID = "offerlens"
BASE = f"https://firestore.googleapis.com/v1/projects/{PROJECT_ID}/databases/(default)/documents"
SITE = "https://offerlens.asadigital.co.in"
SITE_NAME = "OfferLens"
PLAY_URL = "https://play.google.com/store/apps/details?id=com.offerlens.app"

# Offers whose end date has passed are omitted entirely rather than published and
# hidden with CSS - a stale offer that reaches a search index outlives the page.
TODAY_MS = int(datetime.now(timezone.utc).timestamp() * 1000)


# --------------------------------------------------------------------------- fetch

def _fetch(url):
    req = urllib.request.Request(url, headers={"User-Agent": "OfferLens-SiteGen"})
    with urllib.request.urlopen(req, timeout=30) as r:
        return json.loads(r.read().decode("utf-8"))


def _val(v):
    """Unwrap one Firestore REST typed value into a plain Python value."""
    if v is None or "nullValue" in v:
        return None
    if "stringValue" in v:
        return v["stringValue"]
    if "integerValue" in v:
        return int(v["integerValue"])
    if "doubleValue" in v:
        return float(v["doubleValue"])
    if "booleanValue" in v:
        return v["booleanValue"]
    if "arrayValue" in v:
        return [_val(x) for x in v["arrayValue"].get("values", [])]
    if "mapValue" in v:
        return {k: _val(x) for k, x in v["mapValue"].get("fields", {}).items()}
    return None


def load_offers():
    meta = _fetch(f"{BASE}/catalogue/meta")
    chunk_count = int(meta["fields"]["chunkCount"]["integerValue"])
    offers = []
    for i in range(chunk_count):
        chunk = _fetch(f"{BASE}/catalogue/chunk_{i}")
        for entry in chunk["fields"]["offers"]["arrayValue"].get("values", []):
            offers.append({k: _val(v) for k, v in entry["mapValue"]["fields"].items()})
    live = [o for o in offers if not o.get("endDateMs") or o["endDateMs"] >= TODAY_MS]
    dropped = len(offers) - len(live)
    if dropped:
        print(f"  skipped {dropped} expired offer(s)")
    return live


# --------------------------------------------------------------------------- utils

def slugify(*parts):
    s = "-".join(str(p) for p in parts if p)
    s = re.sub(r"[^a-zA-Z0-9]+", "-", s).strip("-").lower()
    return re.sub(r"-{2,}", "-", s) or "offer"


def e(s):
    return html.escape(str(s if s is not None else ""), quote=True)


def money(v):
    if v is None:
        return None
    return f"{int(v):,}" if float(v) == int(v) else f"{v:,}"


def discount_label(o):
    v = o.get("discountValue") or 0
    if str(o.get("discountType", "")).lower() == "percentage":
        return f"{money(v)}% off"
    return f"Rs {money(v)} off"


def fmt_date(ms):
    if not ms:
        return None
    return datetime.fromtimestamp(ms / 1000, tz=timezone.utc).strftime("%d %b %Y")


def iso_date(ms):
    if not ms:
        return None
    return datetime.fromtimestamp(ms / 1000, tz=timezone.utc).strftime("%Y-%m-%d")


def offer_path(o):
    return f"offer/{slugify(o.get('merchant'), o.get('bankName'), o.get('id', '')[:6])}.html"


# ----------------------------------------------------------------------- templates

CSS = """
:root{--bg:#0A1A0A;--card:rgba(26,26,36,.5);--line:rgba(46,125,50,.25);
--orange:#E65100;--green:#2E7D32;--text:#e0e0e0;--dim:#9aa39a}
*{box-sizing:border-box}
body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;
line-height:1.6;color:var(--text);background:var(--bg);margin:0;padding:0}
.wrap{max-width:1000px;margin:0 auto;padding:24px 16px 64px}
a{color:var(--green);text-decoration:none}a:hover{text-decoration:underline}
header.site{border-bottom:1px solid var(--line);margin-bottom:28px;padding-bottom:16px;
display:flex;flex-wrap:wrap;gap:12px;align-items:center;justify-content:space-between}
.brand{font-size:22px;font-weight:800;color:var(--orange);text-decoration:none}
nav.site a{margin-left:16px;font-size:14px;color:var(--dim)}
h1{color:var(--orange);font-size:28px;line-height:1.25;margin:0 0 8px}
h2{color:var(--orange);border-left:4px solid var(--green);padding-left:12px;margin:36px 0 14px;font-size:20px}
.sub{color:var(--dim);margin:0 0 24px;font-size:15px}
.grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(280px,1fr));gap:14px}
.card{background:var(--card);border:1px solid var(--line);border-radius:14px;padding:16px;display:block}
.card:hover{border-color:var(--green);text-decoration:none}
.card .amt{color:var(--orange);font-weight:800;font-size:20px;display:block}
.card .mer{font-weight:700;color:var(--text);margin-top:2px;display:block}
.card .bank{color:var(--dim);font-size:13px;display:block}
.card .desc{color:var(--dim);font-size:13px;margin-top:8px}
.chips{margin:0 0 22px;display:flex;flex-wrap:wrap;gap:8px}
.chip{display:inline-block;background:var(--card);border:1px solid var(--line);
border-radius:999px;padding:5px 13px;font-size:13px;color:var(--text)}
.tag{display:inline-block;border:1px solid var(--line);border-radius:6px;
padding:2px 8px;font-size:12px;color:var(--dim);margin:0 6px 6px 0}
.detail{background:var(--card);border:1px solid var(--line);border-radius:14px;padding:22px;margin-bottom:22px}
.detail dl{display:grid;grid-template-columns:auto 1fr;gap:8px 18px;margin:0}
.detail dt{color:var(--dim);font-size:14px}.detail dd{margin:0;font-size:14px}
.code{font-family:ui-monospace,Menlo,Consolas,monospace;background:rgba(230,81,0,.12);
border:1px dashed var(--orange);color:var(--orange);border-radius:8px;padding:4px 12px;
display:inline-block;font-weight:700;letter-spacing:.5px}
.btn{display:inline-block;background:var(--orange);color:#fff!important;font-weight:700;
border-radius:10px;padding:11px 20px;margin:6px 8px 0 0}
.btn:hover{text-decoration:none;opacity:.92}
.btn.alt{background:transparent;color:var(--green)!important;border:1px solid var(--green)}
.cta{background:var(--card);border:1px solid var(--line);border-radius:14px;
padding:20px;margin:34px 0;text-align:center}
footer.site{border-top:1px solid var(--line);margin-top:44px;padding-top:18px;
color:var(--dim);font-size:12.5px}
footer.site a{color:var(--dim);text-decoration:underline}
.disc{background:rgba(230,81,0,.07);border:1px solid rgba(230,81,0,.25);
border-radius:10px;padding:12px 14px;font-size:12.5px;color:var(--dim);margin:22px 0}
@media(max-width:520px){h1{font-size:23px}.detail dl{grid-template-columns:1fr;gap:2px 0}
.detail dt{margin-top:10px}}
"""

# Shown on every page. Affiliate disclosure is a legal requirement in most
# jurisdictions and a condition of most affiliate networks' terms - it needs to be
# present from the first crawl, not retrofitted once links start earning.
DISCLAIMER = (
    "OfferLens is an independent aggregator. We are not affiliated with, endorsed by, "
    "or sponsored by any bank, card issuer, or merchant listed. All trademarks belong to "
    "their respective owners. Offers are collected from official issuer websites and may "
    "change or expire without notice - always confirm the current terms on the issuer's "
    "own page before transacting. Some outbound links may earn us a commission at no "
    "extra cost to you."
)


def page(title, desc, canonical, body, extra_head=""):
    return f"""<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>{e(title)}</title>
<meta name="description" content="{e(desc)}">
<link rel="canonical" href="{SITE}/{canonical}">
<meta property="og:type" content="website">
<meta property="og:title" content="{e(title)}">
<meta property="og:description" content="{e(desc)}">
<meta property="og:url" content="{SITE}/{canonical}">
<meta property="og:site_name" content="{SITE_NAME}">
<meta name="twitter:card" content="summary">
<style>{CSS}</style>
{extra_head}
</head>
<body>
<div class="wrap">
<header class="site">
  <a class="brand" href="{SITE}/">OfferLens</a>
  <nav class="site">
    <a href="{SITE}/">All offers</a>
    <a href="{SITE}/banks.html">Banks</a>
    <a href="{PLAY_URL}" rel="nofollow">Get the app</a>
  </nav>
</header>
{body}
<div class="cta">
  <strong style="color:var(--orange)">Get every offer for the cards you actually hold</strong>
  <p style="color:var(--dim);font-size:14px;margin:8px 0 14px">
    OfferLens filters offers to your own cards, so you only see deals you can actually use.</p>
  <a class="btn" href="{PLAY_URL}" rel="nofollow">Download the Android app</a>
</div>
<div class="disc">{DISCLAIMER}</div>
<footer class="site">
  <p>&copy; {datetime.now().year} OfferLens &middot;
     <a href="{SITE}/privacy/offerlens.html">Privacy</a> &middot;
     <a href="{SITE}/terms-and-conditions.html">Terms</a> &middot;
     <a href="{SITE}/merchant-opt-out.html">Merchant opt-out</a></p>
</footer>
</div>
</body>
</html>
"""


def card(o, prefix=""):
    bits = [f'<a class="card" href="{prefix}{offer_path(o)}">',
            f'<span class="amt">{e(discount_label(o))}</span>',
            f'<span class="mer">{e(o.get("merchant"))}</span>',
            f'<span class="bank">{e(o.get("bankName"))}</span>']
    if o.get("description"):
        d = o["description"]
        bits.append(f'<div class="desc">{e(d[:110])}{"&hellip;" if len(d) > 110 else ""}</div>')
    bits.append("</a>")
    return "".join(bits)


def offer_jsonld(o):
    """schema.org/Offer - what earns rich results for the offer pages."""
    data = {
        "@context": "https://schema.org",
        "@type": "Offer",
        "name": f"{discount_label(o)} at {o.get('merchant')} with {o.get('bankName')}",
        "description": o.get("description") or "",
        "seller": {"@type": "Organization", "name": o.get("bankName")},
        "category": o.get("category") or "",
        "url": f"{SITE}/{offer_path(o)}",
        "availability": "https://schema.org/InStock",
    }
    if o.get("endDateMs"):
        data["validThrough"] = iso_date(o["endDateMs"])
    if o.get("couponCode"):
        data["offeredBy"] = {"@type": "Organization", "name": o.get("bankName")}
    return ('<script type="application/ld+json">'
            + json.dumps(data, ensure_ascii=False) + "</script>")


# --------------------------------------------------------------------------- build

def write(out, rel, content):
    path = os.path.join(out, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)


def build(out):
    print("Fetching published catalogue...")
    offers = load_offers()
    if not offers:
        print("No live offers found - nothing to build.", file=sys.stderr)
        return 1
    print(f"  {len(offers)} live offer(s)")

    offers.sort(key=lambda o: (o.get("createdAtMs") or 0), reverse=True)

    banks, cats = {}, {}
    for o in offers:
        if o.get("bankName"):
            banks.setdefault(o["bankName"], []).append(o)
        if o.get("category"):
            cats.setdefault(o["category"], []).append(o)

    urls = []

    # ---- offer detail pages (the pages that rank for long-tail searches)
    for o in offers:
        rel = offer_path(o)
        title = f"{discount_label(o)} at {o.get('merchant')} with {o.get('bankName')} | OfferLens"
        desc = o.get("description") or f"{discount_label(o)} at {o.get('merchant')} using {o.get('bankName')} cards."

        rows = [("Bank / Issuer", e(o.get("bankName"))),
                ("Merchant", e(o.get("merchant"))),
                ("Discount", e(discount_label(o)))]
        if o.get("maxDiscountAmount"):
            rows.append(("Maximum discount", f"Rs {money(o['maxDiscountAmount'])}"))
        if o.get("minOrderValue"):
            rows.append(("Minimum order", f"Rs {money(o['minOrderValue'])}"))
        if o.get("paymentType"):
            rows.append(("Payment type", e(o["paymentType"])))
        if o.get("category"):
            rows.append(("Category", e(o["category"])))
        rows.append(("Valid until", fmt_date(o.get("endDateMs")) or "No expiry published"))

        if o.get("couponCode"):
            code = f'<span class="code">{e(o["couponCode"])}</span>'
        elif o.get("couponRevealedOnSite"):
            code = "Shown on the issuer's site after you sign in"
        else:
            code = "No code needed"
        rows.append(("Coupon code", code))

        dl = "".join(f"<dt>{k}</dt><dd>{v}</dd>" for k, v in rows)

        tiers = ""
        if o.get("tiers"):
            items = "".join(
                f"<li><strong>{e(t.get('label'))}</strong> &mdash; "
                f"{money(t.get('discountValue'))}"
                f"{'%' if str(o.get('discountType','')).lower()=='percentage' else ''}"
                + (f", up to Rs {money(t['maxDiscountAmount'])}" if t.get("maxDiscountAmount") else "")
                + (f", min order Rs {money(t['minOrderValue'])}" if t.get("minOrderValue") else "")
                + (f" &middot; {e(t['note'])}" if t.get("note") else "")
                + "</li>"
                for t in o["tiers"])
            tiers = f"<h2>Offer tiers</h2><ul>{items}</ul>"

        terms = ""
        if o.get("termsAndConditions"):
            terms = f"<h2>Key terms</h2><p>{e(o['termsAndConditions'])}</p>"

        links = ""
        if o.get("merchantUrl"):
            links += f'<a class="btn" href="{e(o["merchantUrl"])}" rel="nofollow sponsored noopener" target="_blank">Go to {e(o.get("merchant"))}</a>'
        if o.get("offerSourceUrl"):
            links += f'<a class="btn alt" href="{e(o["offerSourceUrl"])}" rel="nofollow noopener" target="_blank">Verify on {e(o.get("bankName"))}</a>'

        related = [x for x in banks.get(o.get("bankName"), []) if x["id"] != o["id"]][:6]
        rel_html = ""
        if related:
            rel_html = (f'<h2>More {e(o.get("bankName"))} offers</h2>'
                        f'<div class="grid">{"".join(card(r, "../") for r in related)}</div>')

        body = (f"<h1>{e(discount_label(o))} at {e(o.get('merchant'))}</h1>"
                f'<p class="sub">With {e(o.get("bankName"))} cards'
                + (f" &middot; valid until {fmt_date(o['endDateMs'])}" if o.get("endDateMs") else "")
                + "</p>"
                f'<div class="detail"><dl>{dl}</dl></div>'
                + (f"<p>{e(o['description'])}</p>" if o.get("description") else "")
                + tiers + terms
                + (f"<p>{links}</p>" if links else "")
                + rel_html)

        # Detail pages sit one directory deep; site chrome uses absolute URLs and the
        # in-page cards are built with a "../" prefix, so nothing needs rewriting here.
        write(out, rel, page(title, desc, rel, body, offer_jsonld(o)))
        urls.append((rel, o.get("endDateMs")))

    # ---- per-bank pages ("HDFC Bank offers" is a real, repeated search)
    for bank, items in sorted(banks.items()):
        rel = f"bank/{slugify(bank)}.html"
        title = f"{bank} Offers & Discounts ({datetime.now().strftime('%B %Y')}) | OfferLens"
        desc = (f"{len(items)} current {bank} card offers - verified from {bank}'s official site. "
                f"Discounts on {', '.join(sorted({i['merchant'] for i in items})[:5])} and more.")
        body = (f"<h1>{e(bank)} offers</h1>"
                f'<p class="sub">{len(items)} live offer(s), collected from {e(bank)}\'s official website.</p>'
                f'<div class="grid">{"".join(card(i, "../") for i in items)}</div>')
        write(out, rel, page(title, desc, rel, body))
        urls.append((rel, None))

    # ---- per-category pages
    for cat, items in sorted(cats.items()):
        rel = f"category/{slugify(cat)}.html"
        title = f"{cat} Offers on Credit & Debit Cards ({datetime.now().strftime('%B %Y')}) | OfferLens"
        desc = f"{len(items)} current {cat.lower()} offers across Indian bank cards and UPI apps."
        body = (f"<h1>{e(cat)} offers</h1>"
                f'<p class="sub">{len(items)} live {e(cat.lower())} offer(s) across Indian banks and wallets.</p>'
                f'<div class="grid">{"".join(card(i, "../") for i in items)}</div>')
        write(out, rel, page(title, desc, rel, body))
        urls.append((rel, None))

    # ---- bank index
    rows = "".join(
        f'<a class="chip" href="bank/{slugify(b)}.html">{e(b)} ({len(v)})</a>'
        for b, v in sorted(banks.items()))
    write(out, "banks.html", page(
        "All Banks & Card Issuers | OfferLens",
        "Browse current card offers by bank - HDFC, SBI, ICICI, Axis, Kotak, RBL, Bank of Baroda and more.",
        "banks.html",
        f"<h1>Offers by bank</h1><p class=\"sub\">Every issuer we currently track.</p><div class=\"chips\">{rows}</div>"))
    urls.append(("banks.html", None))

    # ---- home
    cat_chips = "".join(
        f'<a class="chip" href="category/{slugify(c)}.html">{e(c)} ({len(v)})</a>'
        for c, v in sorted(cats.items()))
    bank_chips = "".join(
        f'<a class="chip" href="bank/{slugify(b)}.html">{e(b)}</a>'
        for b, v in sorted(banks.items(), key=lambda kv: -len(kv[1]))[:12])
    home = (f"<h1>Credit &amp; debit card offers in India</h1>"
            f'<p class="sub">{len(offers)} live offers across {len(banks)} banks and wallets, '
            f'each collected from the issuer\'s own website and dated.</p>'
            f"<h2>Browse by category</h2><div class=\"chips\">{cat_chips}</div>"
            f"<h2>Browse by bank</h2><div class=\"chips\">{bank_chips}</div>"
            f"<h2>Latest offers</h2><div class=\"grid\">{''.join(card(o) for o in offers[:48])}</div>")
    write(out, "index.html", page(
        f"Credit Card Offers in India ({datetime.now().strftime('%B %Y')}) | OfferLens",
        f"{len(offers)} verified credit and debit card offers from {len(banks)} Indian banks and UPI apps. "
        "Collected from official issuer websites, with dates and terms.",
        "", home))
    urls.append(("", None))

    # ---- sitemap + robots
    today = datetime.now().strftime("%Y-%m-%d")
    entries = "".join(
        f"<url><loc>{SITE}/{u}</loc><lastmod>{today}</lastmod></url>" for u, _ in urls)
    write(out, "sitemap.xml",
          '<?xml version="1.0" encoding="UTF-8"?>'
          '<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">'
          + entries + "</urlset>")
    # The admin panel must never be crawled; it is protected by auth, but keeping it
    # out of the index avoids advertising its location.
    write(out, "robots.txt",
          f"User-agent: *\nAllow: /\nDisallow: /admin/\n\nSitemap: {SITE}/sitemap.xml\n")

    print(f"\nWrote {len(urls) + 1} file(s) to {out}")
    print(f"  {len(offers)} offer pages, {len(banks)} bank pages, {len(cats)} category pages")
    print("\nUpload the CONTENTS of this folder to /var/www/offerlens/ on the VPS.")
    print("Do not delete admin/, privacy/ or the existing legal pages - this only adds files.")
    return 0


if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    ap.add_argument("--out", default="web_out")
    a = ap.parse_args()
    sys.exit(build(a.out))
