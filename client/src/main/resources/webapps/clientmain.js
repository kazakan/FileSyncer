uploadList = [];

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
function addToUploadList(path) {
    for (var i = 0; i < uploadList.length; ++i) {
        if (uploadList[i] == path) {
            return;
        }
    }
    uploadList.push(path);

    updateUploadListHtml();
}

function updateUploadListHtml() {
    var listElement = document.getElementById("upload-list");
    listElement.innerHTML = "";
    for (var i = 0; i < uploadList.length; ++i) {
        listElement.innerHTML += uploadListRow(uploadList[i]);
    }
}

function removeFromUploadList(path) {
    if (uploadList.includes(path)) {
        var idx = uploadList.indexOf(path);
        uploadList.splice(idx, 1);

        updateUploadListHtml();

        return;
    }
}

function upload() {
    for (var i = 0; i < uploadList.length; ++i) {
        uploadFile(uploadList[i]);
    }
    uploadList = [];
    updateUploadListHtml();
}

function uploadListRow(path) {
    var ret = '<div class="uploadRow">';
    ret += path;
    ret +=
        '<button style="float: right" onclick="removeFromUploadList(\'' +
        path +
        "')\">x</button>";
    ret += "</div>";
    return ret;
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
                    var isLocal =
                        result[i]["status"] == "Both" ||
                        result[i]["status"] == "Local Only";
                    var isCloud =
                        result[i]["status"] == "Both" ||
                        result[i]["status"] == "Cloud Only";

                    var row = '<div class="row">' + result[i]["name"];

                    // download button
                    row +=
                        '<button style="float:right;" disabled="">download</button>';

                    // add to upload list button
                    row +=
                        "<button onclick=\"addToUploadList('" +
                        result[i]["name"] +
                        '\')" style="float:right;" ';
                    if (!isLocal) row += 'disabled=""';
                    row += ">Select</button>";
                    row +=
                        '<span style="float:right; margin-right:5px;">' +
                        result[i]["status"] +
                        "</span>";

                    row += "</div> \n";
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
    var messageWindow = document.getElementById("report-list");
    messageWindow.innerHTML += "<p>" + msg + "</p>";
    messageWindow.scrollTo(0, messageWindow.scrollHeight);
}

function getReportMessage() {
    axios.get("/api/msgSse", {}).then((response) => {
        messages = response.data;
        msgList = messages.split("\n");
        for (var i = 0; i < msgList.length; ++i) {
            addReportMessage(msgList[i]);
        }
    });
}

function onLoad() {
    getFileList("/");
    setInterval(getReportMessage, 1000);
}
