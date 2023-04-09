function onlogin() {
  var username = document.getElementById("username").value;
  var password = document.getElementById("password").value;

  axios
    .post("/api/loginReq", {
      username: username,
      password: password,
    })
    .then(
      (response) => {
        var result = response.data;
        console.log("Processing Request");
        console.log(result);
      },
      (error) => {}
    );
}
