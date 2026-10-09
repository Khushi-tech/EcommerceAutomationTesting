import os
from pathlib import Path
try:
    import xlwt
except ImportError:
    raise SystemExit('xlwt library is required to write .xls files')

folder = Path('src/test/resources/testdata')
folder.mkdir(exist_ok=True)
workbook = xlwt.Workbook()
sheet = workbook.add_sheet('Sheet1')

rows = [
    ['BookName', 'PurchasedDate', 'Amount', 'Location'],
    ['The Silent Patient', '12-Jan-26', '499', 'Delhi'],
    ['Atomic Habits', '#', '650', 'Mumbai'],
    ['The Alchemist', '8-Mar-26', '350', 'Noida'],
    ['Rich Dad Poor Dad', '19-Apr-26', '575', 'Bengaluru'],
]

for r, row in enumerate(rows):
    for c, value in enumerate(row):
        sheet.write(r, c, value)

output_path = folder / 'test_data.xls'
workbook.save(output_path)
print(f'Created {output_path.resolve()}')

# Create login test data with positive and negative combinations
login_rows = [
    ['username', 'password', 'expected'],
    # Positive case
    ['${TEST_USERNAME}', '${TEST_PASSWORD}', 'PASS'],
    # Invalid password
    ['standard_user', 'wrong_password', 'FAIL'],
    # Invalid username
    ['locked_out_user', 'secret_sauce', 'FAIL'],
    # Empty username
    ['', 'secret_sauce', 'FAIL'],
    # Empty password
    ['standard_user', '', 'FAIL'],
    # Special characters
    ['!@#$%', '12345', 'FAIL'],
    # SQL injection-like input
    ["admin' --", 'password', 'FAIL'],
]

login_workbook = xlwt.Workbook()
login_sheet = login_workbook.add_sheet('Sheet1')

for r, row in enumerate(login_rows):
    for c, value in enumerate(row):
        login_sheet.write(r, c, value)

login_output_path = folder / 'login_test_data_generated.xls'
login_workbook.save(login_output_path)
print(f'Created {login_output_path.resolve()}')
