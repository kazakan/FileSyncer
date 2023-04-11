function onlogin() {
  var username = document.getElementById("username").value;
  var password = document.getElementById("password").value;
  var address = document.getElementById("address").value;
  var port = document.getElementById("port").value;

  var params = new URLSearchParams();
  params.append("username", username);
  params.append("password", password);
  params.append("address", address);
  params.append("port", port);

  axios.post("/api/loginReq", params).then(
    (response) => {
      var result = response.data;
      if (result.result == "ok") {
        window.location = "/fs/clmain";
      } else {
        alert("rejected");
      }
    },
    (error) => {}
  );
}

function onRegister() {
  var username = document.getElementById("username").value;
  var password = document.getElementById("password").value;
  var address = document.getElementById("address").value;
  var port = document.getElementById("port").value;

  var params = new URLSearchParams();
  params.append("username", username);
  params.append("password", password);
  params.append("address", address);
  params.append("port", port);

  axios.post("/api/registerReq", params).then(
    (response) => {
      var result = response.data;
      if (result.result == "ok") {
        alert("User '" + username + "' registered.");
      } else {
        alert("Failed register user '" + username + "'");
      }
    },
    (error) => {}
  );
}
