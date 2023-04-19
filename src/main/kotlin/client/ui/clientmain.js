function uploadFile(path) {
  var params = new URLSearchParams();
  params.append("fname", path);

  axios.post("/api/uploadFile", params).then(
    (response) => {
      getFileList("");
    },
    (error) => {
      alert(error);
    }
  );
}

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
            "<button onclick=\"uploadFile('" +
            result[i]["name"] +
            '\')" style="float:right;">upload</button>' +
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

function addReportMessage(msg) {
  var messageWindow = document.getElementById("right-panel");
  messageWindow.innerHTML += "<p>" + msg + "</p>";
  messageWindow.scrollTo(0, messageWindow.scrollHeight);
}

function onLoad() {
  getFileList("/");
  const eventSource = new EventSource("/api/msgSse");

  eventSource.addEventListener("message", (event) => {
    addReportMessage(event.data);
  });
}
