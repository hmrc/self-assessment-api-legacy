The Making Tax Digital Self Assessment API allows software packages to provide a taxpayer's financial data for 
their self-employment and UK property businesses, and other personal income, and to request and view their tax 
calculation. This data includes income, expenses, and allowances etc. for each business, as well as personal savings 
interest and dividends. To meet their obligations, taxpayers are required to submit regular financial updates for 
each of their businesses. Typically these updates cover four 3-month _obligation periods_, with one set of 
_obligation periods_ per business per accounting period. To meet the obligation for a particular _obligation period_
for a business, taxpayers are required to:
* Supply summarised transactions for income and expenditure for the the whole of the _obligation period_ for that specific business
* Request a tax calculation

The data for each _obligation period_ can be provided either as a single _update period_, 
or as multiple smaller _update periods_, covering the whole period so long as there are no gaps, and no overlaps 
among the _update periods_.
More accurate tax calculations can be provided when businesses' allowances and adjustments, and personal savings 
and dividend income are also provided, though these details are not required to meet obligations.

### Unauthorised Agents (Filing Only Agents) ###

Where an Agent is not authorised by the taxpayer to fully represent the taxpayer, then the Agent is only allowed to send data to HMRC. These Agents are referred to as Filing Only Agents, meaning that they can submit (file) taxpayer data to HMRC, but cannot retrieve existing data.

* GET requests are rejected
* POST and PUT requests are processed normally; however, in case of a business validation error, a generic error is returned

Note: The above restrictions mean it is not possible for an unauthorised Agent to use the APIs to obtain certain data necessary to submit an update e.g. pre-existing business identifiers, pre-existing periodic update identifiers, so these must be recorded on creation, or obtained from a source that does have access e.g. the taxpayer.

### Notes ###
* This API is usable only for taxpayers subscribed to Making Tax Digital (MTD) and only for tax years 2017/18 onwards.
* Self-employment and UK property businesses may be defined as part of the MTD subscription process.
* Some APIs may be marked "test only"; this means that they are not available for use in production.
