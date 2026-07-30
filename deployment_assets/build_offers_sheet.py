"""Generate the bulk-upload sheet for the launch offer catalogue.

Column headers must match the parser in public/admin/manage-offers.html exactly -
processBulkData() reads item['Merchant'], item['Bank Name'], etc. by name.
"""
from openpyxl import Workbook
from openpyxl.styles import Font, PatternFill, Alignment
from openpyxl.utils import get_column_letter

HEADERS = [
    "Merchant", "Bank Name", "Category", "Payment Type",
    "Discount Type", "Discount Value", "Max Discount", "Min Order",
    "Description", "Terms & C", "Coupon Code", "Code On Site",
    "Merchant URL", "Offer Source", "Valid Until",
]

SBI_SRC = "https://www.sbicard.com/en/personal/offer/"
ICICI_SRC = "https://www.icici.bank.in/offers/"

ROWS = [
    # --- SBI Card ---
    dict(Merchant="BigBasket", **{"Bank Name": "SBI Card"}, Category="Groceries",
         **{"Payment Type": "Credit Card"}, **{"Discount Type": "Percentage"},
         **{"Discount Value": 10}, **{"Max Discount": 250}, **{"Min Order": 999},
         Description="10% instant discount on groceries at BigBasket. Valid 26th to month-end, once per card per month.",
         **{"Terms & C": ""}, **{"Coupon Code": "SBICC"}, **{"Code On Site": ""},
         **{"Merchant URL": "https://www.bigbasket.com"},
         **{"Offer Source": SBI_SRC + "big-basket-29july26.page"},
         **{"Valid Until": "2026-09-30"}),

    dict(Merchant="Apple", **{"Bank Name": "SBI Card"}, Category="Shopping",
         **{"Payment Type": "Credit Card"}, **{"Discount Type": "Flat"},
         **{"Discount Value": 15000}, **{"Max Discount": ""}, **{"Min Order": ""},
         Description="Up to Rs 15,000 instant discount on iPhone, MacBook, iPad, Watch and Pods. EMI transactions.",
         **{"Terms & C": ""}, **{"Coupon Code": ""}, **{"Code On Site": ""},
         **{"Merchant URL": "https://www.apple.com/in/"},
         **{"Offer Source": SBI_SRC + "apple-9july26.page"},
         **{"Valid Until": "2026-09-26"}),

    dict(Merchant="IGP", **{"Bank Name": "SBI Card"}, Category="Shopping",
         **{"Payment Type": "Credit Card"}, **{"Discount Type": "Percentage"},
         **{"Discount Value": 20}, **{"Max Discount": ""}, **{"Min Order": 1199},
         Description="20% instant discount on gifts and flowers at IGP.",
         **{"Terms & C": ""}, **{"Coupon Code": "SBIIGP20"}, **{"Code On Site": ""},
         **{"Merchant URL": "https://www.igp.com"},
         **{"Offer Source": SBI_SRC + "igp-1june26.page"},
         **{"Valid Until": "2027-03-31"}),

    dict(Merchant="ClearTax", **{"Bank Name": "SBI Card"}, Category="Shopping",
         **{"Payment Type": "Credit Card"}, **{"Discount Type": "Percentage"},
         **{"Discount Value": 75}, **{"Max Discount": ""}, **{"Min Order": ""},
         Description="Flat 75% off ClearTax income tax filing plans, self-filing and expert-assisted.",
         **{"Terms & C": ""}, **{"Coupon Code": ""}, **{"Code On Site": "Yes"},
         **{"Merchant URL": "https://cleartax.in"},
         **{"Offer Source": SBI_SRC + "clear-tax-4july26.page"},
         **{"Valid Until": "2027-03-31"}),

    dict(Merchant="MediBuddy", **{"Bank Name": "SBI Card"}, Category="Shopping",
         **{"Payment Type": "Credit Card"}, **{"Discount Type": "Flat"},
         **{"Discount Value": 3600}, **{"Max Discount": ""}, **{"Min Order": ""},
         Description="3 months unlimited online doctor consultations for 6 family members at Rs 1,399 instead of Rs 4,999.",
         **{"Terms & C": ""}, **{"Coupon Code": "SBICMBG"}, **{"Code On Site": ""},
         **{"Merchant URL": "https://www.medibuddy.in"},
         **{"Offer Source": SBI_SRC + "medibuddy-16july26.page"},
         **{"Valid Until": "2027-03-31"}),

    dict(Merchant="MediBuddy", **{"Bank Name": "SBI Card"}, Category="Shopping",
         **{"Payment Type": "Credit Card"}, **{"Discount Type": "Flat"},
         **{"Discount Value": 600}, **{"Max Discount": ""}, **{"Min Order": ""},
         Description="Full body health checkup with 85 parameters at Rs 1,899 instead of Rs 2,499.",
         **{"Terms & C": ""}, **{"Coupon Code": "SBIC600"}, **{"Code On Site": ""},
         **{"Merchant URL": "https://www.medibuddy.in"},
         **{"Offer Source": SBI_SRC + "medibuddy-16july26.page"},
         **{"Valid Until": "2027-03-31"}),

    dict(Merchant="Flipkart", **{"Bank Name": "SBI Card"}, Category="Shopping",
         **{"Payment Type": "Credit Card"}, **{"Discount Type": "Percentage"},
         **{"Discount Value": 10}, **{"Max Discount": 1000}, **{"Min Order": 4990},
         Description="10% instant discount on Flipkart, EMI transactions only. Cap Rs 750 on mobiles, Rs 1,000 other categories.",
         **{"Terms & C": ""}, **{"Coupon Code": ""}, **{"Code On Site": ""},
         **{"Merchant URL": "https://www.flipkart.com"},
         **{"Offer Source": SBI_SRC + "flipkart-28july26.page"},
         **{"Valid Until": "2026-07-31"}),

    dict(Merchant="Amazon", **{"Bank Name": "SBI Card"}, Category="Shopping",
         **{"Payment Type": "Credit Card"}, **{"Discount Type": "Percentage"},
         **{"Discount Value": 10}, **{"Max Discount": 1000}, **{"Min Order": 5000},
         Description="10% instant discount on Amazon, EMI transactions only. Cap Rs 1,000 per transaction, Rs 4,000 per card.",
         **{"Terms & C": ""}, **{"Coupon Code": ""}, **{"Code On Site": ""},
         **{"Merchant URL": "https://www.amazon.in"},
         **{"Offer Source": SBI_SRC + "amazon-15july26.page"},
         **{"Valid Until": "2026-07-31"}),

    # --- ICICI Bank ---
    dict(Merchant="TaxBuddy", **{"Bank Name": "ICICI Bank"}, Category="Shopping",
         **{"Payment Type": "Credit Card, Debit Card"}, **{"Discount Type": "Percentage"},
         **{"Discount Value": 20}, **{"Max Discount": ""}, **{"Min Order": ""},
         Description="20% off ITR filing, tax planning, notice management and GST services. Also valid on Net Banking.",
         **{"Terms & C": ""}, **{"Coupon Code": ""}, **{"Code On Site": "Yes"},
         **{"Merchant URL": "https://www.taxbuddy.com"},
         **{"Offer Source": ICICI_SRC + "tax-buddy-discount-offer"},
         **{"Valid Until": "2027-04-30"}),

    dict(Merchant="Surat Diamond", **{"Bank Name": "ICICI Bank"}, Category="Shopping",
         **{"Payment Type": "Credit Card, Debit Card"}, **{"Discount Type": "Percentage"},
         **{"Discount Value": 20}, **{"Max Discount": ""}, **{"Min Order": 2000},
         Description="20% instant discount on jewellery at Surat Diamond. Excludes bullion, coins and solitaires.",
         **{"Terms & C": ""}, **{"Coupon Code": "ICICIBANK20"}, **{"Code On Site": ""},
         **{"Merchant URL": "https://www.suratdiamond.com"},
         **{"Offer Source": ICICI_SRC + "get-20-percent-instant-discount-at-surat-diamond"},
         **{"Valid Until": "2027-03-31"}),

    dict(Merchant="Carzonrent", **{"Bank Name": "ICICI Bank"}, Category="Travel",
         **{"Payment Type": "Credit Card, Debit Card"}, **{"Discount Type": "Percentage"},
         **{"Discount Value": 30}, **{"Max Discount": ""}, **{"Min Order": ""},
         Description="Flat 30% off all Carzonrent rides. First eligible transaction per card per month. Not valid on EMI.",
         **{"Terms & C": ""}, **{"Coupon Code": ""}, **{"Code On Site": ""},
         **{"Merchant URL": "https://www.carzonrent.com"},
         **{"Offer Source": ICICI_SRC + "offer-on-carzonrent"},
         **{"Valid Until": "2026-09-30"}),

    dict(Merchant="Insight Vacations", **{"Bank Name": "ICICI Bank"}, Category="Travel",
         **{"Payment Type": "Credit Card, Debit Card"}, **{"Discount Type": "Percentage"},
         **{"Discount Value": 10}, **{"Max Discount": 25000}, **{"Min Order": ""},
         Description="10% off Insight Vacations tours, up to Rs 25,000 per person. Offline bookings only, minimum two adults.",
         **{"Terms & C": ""}, **{"Coupon Code": ""}, **{"Code On Site": ""},
         **{"Merchant URL": "https://www.insightvacations.com"},
         **{"Offer Source": ICICI_SRC + "insigh-vacation"},
         **{"Valid Until": "2027-09-30"}),
]

wb = Workbook()
ws = wb.active
ws.title = "Offers"

header_font = Font(name="Arial", bold=True, color="FFFFFF")
header_fill = PatternFill("solid", fgColor="1A1A2E")
body_font = Font(name="Arial")

ws.append(HEADERS)
for c in range(1, len(HEADERS) + 1):
    cell = ws.cell(row=1, column=c)
    cell.font = header_font
    cell.fill = header_fill
    cell.alignment = Alignment(vertical="center")

for r in ROWS:
    ws.append([r.get(h, "") for h in HEADERS])

for row in ws.iter_rows(min_row=2):
    for cell in row:
        cell.font = body_font
        cell.alignment = Alignment(vertical="top", wrap_text=True)

widths = {"Merchant": 18, "Bank Name": 14, "Category": 14, "Payment Type": 22,
          "Discount Type": 14, "Discount Value": 14, "Max Discount": 13,
          "Min Order": 11, "Description": 62, "Terms & C": 20, "Coupon Code": 15,
          "Code On Site": 13, "Merchant URL": 32, "Offer Source": 46, "Valid Until": 13}
for i, h in enumerate(HEADERS, start=1):
    ws.column_dimensions[get_column_letter(i)].width = widths[h]
ws.freeze_panes = "A2"

# Notes sheet - guidance, kept off the data sheet so the parser never sees it.
notes = wb.create_sheet("README")
notes["A1"] = "OfferLens bulk upload - how to use"
notes["A1"].font = Font(name="Arial", bold=True, size=14)
guidance = [
    "",
    "1. Verify every row against its Offer Source URL before uploading. These were researched automatically and may be out of date.",
    "2. Upload via admin panel > Bulk Upload. Only the first sheet ('Offers') is read.",
    "3. Column headers must not be renamed - the parser matches them by exact name.",
    "",
    "Column notes:",
    "  Bank Name      Must match the Smart Wallet list exactly, or card filtering won't work.",
    "  Category       Must be one of the app's 7 categories, or the offer appears under no tab.",
    "  Discount Type  Percentage / Flat / Cashback.",
    "  Max Discount   The cap for 'X% off up to Rs Y'. Leave blank if uncapped.",
    "  Min Order      Leave blank rather than 0 when there is no minimum.",
    "  Terms & C      Left blank deliberately. Use 'Generate from offer details' per offer",
    "                 after upload, or write your own summary - do not paste the bank's text.",
    "  Code On Site   'Yes' when a code is needed but only shown after login on the bank's site.",
    "  Offer Source   The bank's page. Merchant URL is the merchant's own site.",
    "  Valid Until    YYYY-MM-DD.",
    "",
    "Not supported by bulk upload: multi-tier offer breakdowns. Add those via the manual form.",
    "",
    "Rows 7-8 (Flipkart, Amazon) expire 31 July 2026 and are EMI-only - drop them if you launch later.",
]
for i, line in enumerate(guidance, start=2):
    notes[f"A{i}"] = line
    notes[f"A{i}"].font = Font(name="Arial", bold=line.endswith(":"))
notes.column_dimensions["A"].width = 120

out = "deployment_assets/offerlens_bulk_offers.xlsx"
wb.save(out)
print("wrote", out, "rows:", len(ROWS))
