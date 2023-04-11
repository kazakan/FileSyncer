function uploadFile(path) {}

function downloadFile(path) {}

function getFileList(dir) {
  var params = new URLSearchParams();
  params.append("dir", dir);
  axios.get("/api/showFolder", { params }).then(
    (response) => {
      var element = document.getElementById("file-list");
      var result = response.data;

      console.log(result[0]);

      element.innerHTML = "";

      if (result.length > 0) {
        for (var i = 0; i < result.length; ++i) {
          var row =
            '<div class="row">' +
            result[i]["name"] +
            '<button style="float:right;">download</button>' +
            '<button style="float:right;">upload</button>' +
            '<span style="float:right; margin-right:5px;">' +
            result[i]["status"] +
            "</span>" +
            "</div> \n";
          element.innerHTML += row;
        }
      } else {
        element.innerHTML += "<div>No files to show</div>";
      }
    },
    (error) => {}
  );
}

function disconnect() {
  axios.post("/api/disconnect", {}).then(
    (response) => {
      alert("Disconnected successfully.");
      window.location = "/fs/login";
    },
    (error) => {
      alert("Something went wrong during disconnecting TT");
    }
  );
}
