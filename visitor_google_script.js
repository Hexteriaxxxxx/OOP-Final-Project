// PUP Santa Rosa - Visitor Request Form Script
// Auto-sends confirmation email when form is submitted
// Auto-sends approval email when admin approves

var SERVER_URL = "http://10.10.117.176:5055/submit-visitor";

function onFormSubmit(e) {
  try {
    var responses = e.response.getItemResponses();
    var data = {};

    responses.forEach(function(r) {
      var title = r.getItem().getTitle().toLowerCase().trim();
      var value = r.getResponse();

      if (title.includes("full name"))         data.visitor_name  = value;
      else if (title.includes("company"))      data.company       = value;
      else if (title.includes("contact"))      data.contact       = value;
      else if (title.includes("email"))        data.email         = value;
      else if (title.includes("host"))         data.host_employee = value;
      else if (title.includes("visit date"))   data.visit_date    = formatDate(value);
      else if (title.includes("time in"))      data.time_in       = formatTime(value);
      else if (title.includes("time out"))     data.time_out      = formatTime(value);
      else if (title.includes("purpose"))      data.purpose       = value;
    });

    data.visit_date = data.visit_date || getTodayDate();
    data.time_in    = data.time_in    || "09:00";
    data.time_out   = data.time_out   || "17:00";
    data.company    = data.company    || "";
    data.contact    = data.contact    || "";

    var options = {
      method: "post",
      contentType: "application/json",
      payload: JSON.stringify(data),
      muteHttpExceptions: true
    };

    var response = UrlFetchApp.fetch(SERVER_URL, options);
    var result   = JSON.parse(response.getContentText());

    if (result.success) {
      Logger.log("Visitor submitted: " + result.request_id + " - " + data.visitor_name);
      sendConfirmationEmail(data, result.request_id);
    } else {
      Logger.log("Server error: " + result.error);
    }

  } catch (err) {
    Logger.log("Script error: " + err.toString());
  }
}

function sendConfirmationEmail(data, requestId) {
  if (!data.email || data.email === "") return;

  var subject = "Visitor Request Received - " + requestId;
  var body =
    "Dear " + data.visitor_name + ",\n\n" +
    "Your visitor request has been RECEIVED and is now pending approval.\n\n" +
    "REFERENCE NUMBER: " + requestId + "\n\n" +
    "Name        : " + data.visitor_name     + "\n" +
    "Company     : " + (data.company || "-") + "\n" +
    "Purpose     : " + data.purpose          + "\n" +
    "Visit Date  : " + data.visit_date       + "\n" +
    "Host        : " + data.host_employee    + "\n\n" +
    "You will receive another email once your request is APPROVED.\n" +
    "Please save your reference number: " + requestId + "\n\n" +
    "Thank you!\n" +
    "PUP Santa Rosa - Pass Slip System";

  GmailApp.sendEmail(data.email, subject, body);
  Logger.log("Email sent to: " + data.email);
}

function formatTime(value) {
  try {
    if (!value) return "09:00";
    if (typeof value === "string" && value.includes(":")) {
      var parts = value.split(":");
      return pad(parseInt(parts[0])) + ":" + pad(parseInt(parts[1]));
    }
    if (Array.isArray(value)) {
      return pad(value[0]) + ":" + pad(value[1]);
    }
    return value.toString();
  } catch(e) {
    return "09:00";
  }
}

function pad(n) {
  return n < 10 ? "0" + n : "" + n;
}

function formatDate(dateValue) {
  if (!dateValue) return getTodayDate();
  try {
    var d = new Date(dateValue);
    return Utilities.formatDate(d, "Asia/Manila", "yyyy-MM-dd");
  } catch(e) {
    return getTodayDate();
  }
}

function getTodayDate() {
  return Utilities.formatDate(new Date(), "Asia/Manila", "yyyy-MM-dd");
}
