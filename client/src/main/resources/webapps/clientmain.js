uploadList = [];

fileList = [];

userList = [];

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
            fileList = result;

            console.log(result[0]);

            element.innerHTML = "";

            if (result.length > 0) {
                for (var i = 0; i < result.length; ++i) {
                    var row = '<div class="row">' + result[i]["name"];

                    // owner text
                    row +=
                        '<span style="float:right; margin-right:5px;">' +
                        result[i]["owner"] +
                        "</span>";

                    // share button
                    row += '<button style="float: right; margin-right: 5px';
                    row += 'onclick = "openShareDialogue()"';
                    row += ">Share</button>";

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

function getShareableUsers(fname) {
    var fileInfo = null;
    for (var i = 0; i < fileList.length; ++i) {
        if (fileList[i]["name"] == fname) {
            fileInfo = fileList[i];
            break;
        }
    }

    if (fileInfo == null) return null;
}

/** Get user registered in server.*/
async function getUserList() {
    var response = await axios.get("/api/listUser", {});
    messages = response.data;
    userList = messages.split("\t");
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

function openShareDialogue() {
    var dialoguePage = document.getElementById("share-dialogue-page");
    dialoguePage.classList.remove("hide");
    dialoguePage.classList.add("full");
}

function closeShareDialogue() {
    var dialoguePage = document.getElementById("share-dialogue-page");
    dialoguePage.classList.remove("full");
    dialoguePage.classList.add("hide");
}

function onLoad() {
    getFileList("/");
    setInterval(getReportMessage, 1000);
    setInterval(function () {
        getFileList("");
    }, 4000);
}
