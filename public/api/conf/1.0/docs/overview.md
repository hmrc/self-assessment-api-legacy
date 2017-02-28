This API allows software packages to provide sole trader's financial data for each of their self-employment businesses 
and/or for their UK property business, including but not limited to income, expense, allowances etc. as well as data for 
other personal income (such as interest from savings, dividends) for a specified tax year (based on the accounting 
period of the business) so that the taxpayer can meet their obligations and/or request a tax calculation.

To meet their obligations, taxpayers are required to submit financial updates during the year for 
each of their self-employment businesses, and/or for their UK property business. Typically these are four 3-month 
obligation periods, one set per business, but there could be for example, an additional shorter obligation period in 
the first year of a business.

To meet an obligation for a particular _obligation period_ for a particular business, taxpayers 
are required to:

* Supply summarised transactions for income and expenditure for the the whole of the _obligation period_ for that 
specific business.
* Request a tax calculation

The data for each obligation period can be provided either as a single _update period_, or as multiple smaller 
_update periods_, covering the whole period, so long as there are no gaps, no overlaps among the _update periods_.

### Unauthorised Agents (Filing Only Agents) ###

Where an Agent is not authorised by the taxpayer to fully represent the taxpayer, then the Agent is only allowed to send data to HMRC. These Agents are referred to as Filing Only Agents, meaning that they can submit (file) taxpayer data to HMRC, but cannot retrieve existing data.

* GET requests are rejected
* POST and PUT requests are processed normally; however, in case of a business validation error, a generic error is returned

Note: The above restrictions mean it is not possible for an unauthorised Agent to use the APIs to obtain certain data necessary to submit an update e.g. pre-existing business identifiers, pre-existing periodic update identifiers, so these must be recorded on creation, or obtained from a source that does have access e.g. the taxpayer.

### Notes ###

* This API is usable only for taxpayers subscribed to Making Tax Digital (MTD) and only for tax years 2017/18 onwards.
* Self-employment and UK property businesses may created as part of the MTD subscription process.
* Some APIs may be marked "test only"; this means that they are not available for use in production.

