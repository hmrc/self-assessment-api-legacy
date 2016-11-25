<!--
1. [Introduction] (#docs-introduction)
2. [Taxpayer] (#docs-taxpayer)
3. [Employment] (#docs-employment)
4. [Self employment] (#docs-self-employment)
5. [UK properties] (#docs-uk-properties)
6. [Unearned incomes] (#docs-unearned-incomes)
7. [Furnished holiday lettings] (#docs-holiday-lettings)
8. [Liabilities] (#docs-liabilities)
-->
This API allows business taxpayers to provide data relating to their business (self-employed, property, partnership) including but not limited to income, expense, allowances etc as well as data for other personal (non-earned) income (such as savings, dividends, capital gains) for a specified tax year (based on the accounting period of the business).


An estimated tax liability will be provided based on the data submitted/sent.

Who is this API useful for?

- Business with annual turnover of £10,000 or more
- Self employed
- Property annual turnover £10,000 or more
- Partnerships
- Trusts

### Taxpayer {#docs-taxpayer}

This sections allows API developers to discover taxpayer’s information and all the resources available under a tax year.

Here, the developer can get the following data:

- Taxpayer’s UTR (Unique Taxpayer Reference is 10 digit number that is unique to the taxpayer).
- All the tax years HMRC holds tax records for
- Liability calculation resource for each of those tax years
- All the Income sources for given tax year.

### Employment {#docs-employment}

This sections allows API developers to provide data relating to PAYE employment income during the specified tax year.

Here, the developer can provide the following data:

- Employment Pay (using P45/60)
- UK Tax paid
- Benefits from the employment (using P11D)
- Employment Expenses

### Self employment {#docs-self-employment}

This section allow API developer to provide data relating to the taxpayer's business(es) that is required to calculate the estimated tax liability.

Here, the developer can provide the following data:

- Business Income
- Business Expenses (total & disallowable by type of expense)
- Tax Allowances (capital allowances) for vehicles & equipment
- Balancing charges
- Goods & Services for own use
- Adjustments

### UK properties {#docs-uk-properties}

This sections allows API developers to provide data relating to taxpayer’s rental income from UK Property, income from letting furnished rooms in own home, premiums from leasing UK land and inducements to take an interest in letting a property (reverse premium).

Here, the developer can provide the following data:

- Property Income
- Property Expenses
- Private Use Adjustment
- Balancing Charges
- Tax Allowances
- Adjustments

\* This section does not include Furnished Holiday Lettings (see below for FHL details)


### Unearned incomes {#docs-unearned-incomes}

This sections allows API developers to provide data relating to Unearned income during the specified tax year.


Here, the developer can provide the following data:

- Savings income
- Dividends
- Benefits

### Furnished holiday lettings {#docs-holiday-lettings}

This sections allows API developers to provide data relating to taxpayer’s income from Furnished Holiday Lettings (FHL) in the UK or European Economic Area (EEA).

Here, the developer can provide the following data:

- FHL Income
- FHL Expenses
- FHL Private Use adjustment
- FHL Balancing Charges
- FHL Capital Allowances
- FHL Adjustments

### Liabilities {#docs-liabilities}

This section lets the API developer get an estimated tax liability based on the data submitted (incomes, expenses etc) so far. This API is asynchronous which means that the developer must request for liabilities computation before actually getting it via another request.

[2015-16 Tax Year Notes](https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/505094/sa103f_notes_2016.pdf)
