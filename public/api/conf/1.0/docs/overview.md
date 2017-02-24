This API allows software packages to provide data relating to sole traders and their businesses including but not limited to income, expense, allowances etc. as well as data for other personal income (such as interest from savings, dividends) for a specified tax year (based on the accounting period of the business).

This API is usable only for taxpayers subscribed to Making Tax Digital (MTD) and only for tax years 2017/18 onwards.

Self-employment and property businesses may created as part of the MTD subscription process.

This API can be used to submit data relating to:
* Self-employment
* Property, including furnished holiday lettings
* Dividends and interest

So that Self-Assessment obligations can be met.

Some APIs may be marked "test only"; this means that they are not available for use in production.

### Unauthorised Agents (Filing Only Agents) ###

Where an Agent is not authorised by the taxpayer to fully represent the taxpayer, then the Agent is only allowed to send data to HMRC. These Agents are referred to as Filing Only Agents, meaning that they can submit (file) taxpayer data to HMRC, but cannot retrieve existing data.

* GET requests are rejected
* POST and PUT requests are processed normally; however, in case of a business validation error, a generic error is returned

Note: The above restrictions mean it is not possible for an unauthorised Agent to use the APIs to obtain certain data necessary to submit an update e.g. pre-existing business identifiers, pre-existing periodic update identifiers, so these must be recorded on creation, or obtained from a source that does have access e.g. the taxpayer.
