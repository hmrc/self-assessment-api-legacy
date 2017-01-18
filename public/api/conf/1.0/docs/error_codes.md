Below are some example errors, for specific errors refer to the individual resources below 


HTTP status : 400
<pre class="snippet--block code_text">
{
  "code": "NINO_INVALID",
  "message": "The provided Nino is invalid"
}
</pre>

<pre class="snippet--block code_text">
{
  "code": "TAX_YEAR_INVALID",
  "message": "Tax year invalid"
}
</pre>

<pre class="snippet--block code_text">
{
  "code": "INVALID_REQUEST",
  "message": "Invalid request",
    "errors": [
    {
      "code": "INVALID_MONETARY_AMOUNT",
      "message": "annualInvestmentAllowance should be non-negative number up to 2 decimal values",
      "path": "/allowances/annualInvestmentAllowance"
    },
    {
      "code": "INVALID_MONETARY_AMOUNT",
      "message": "averagingAdjustment should be non-negative number up to 2 decimal values",
      "path": "/adjustments/averagingAdjustment"
    }
  ]
}
</pre>



HTTP status : 403
<pre class="snippet--block code_text">
{
  "code": "TOO_MANY_SOURCES",
  "message": "The maximum number of Self-Employment incomes sources is 1"
}
</pre>


