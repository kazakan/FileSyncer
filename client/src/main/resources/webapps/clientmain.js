var uploadList = [];

var dummyFileList = [
    { name: "hellofile.txt", owner: "userA", shared: "userB/UserC" },
];
fileList = [];
var tmpFile = null;
var tmpIdx = -1;

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

function getFileListRow(fileData) {
    var row = '<div class="row">' + fileData["name"];

    // owner text
    row +=
        '<span style="float:right; margin-right:5px;">' +
        fileData["owner"] +
        "</span>";

    // share button
    row += '<button style="float: right; margin-right: 5px;" ';
    row += "onclick = \"openShareDialogue('" + fileData["name"] + "')\"";
    row += ">Share</button>";

    row += "</div> \n";

    return row;
}

function getFileList(dir) {
    var params = new URLSearchParams();
    params.append("dir", dir);
    axios.get("/api/showFolder", { params }).then(
        (response) => {
            var element = document.getElementById("file-list");
            var result = response.data;
            fileList = result;

            // just for debug
            if (fileList.length == 0) fileList = dummyFileList;

            console.log(result[0]);

            element.innerHTML = "";

            if (result.length > 0) {
                for (var i = 0; i < result.length; ++i) {
                    var row = getFileListRow(result[i]);
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
function getUserList() {
    axios.get("/api/listUser", {}).then(
        (response) => {
            messages = response.data;
            userList = messages.split("\t");
            console.log(messages);
            console.log(userList);
        },
        (error) => {
            console.log(error);
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

function openShareDialogue(fileName) {
    var dialoguePage = document.getElementById("share-dialogue-page");

    dialoguePage.classList.remove("hide");
    dialoguePage.classList.add("full");

    for (var i = 0; i < fileList.length; ++i) {
        if (fileList[i]["name"] == fileName) {
            tmpIdx = i;
            tmpFile = fileList[i];
            break;
        }
    }

    updateChipContainer();
}

function userInList(userName, list) {
    for (var i = 0; i < list.length; ++i) {
        if (list[i] == userName) return true;
    }
    return false;
}

function checkUserAddable(userName) {
    return (
        userInList(userName, userList) &&
        !userInList(userName, tmpFile["shared"].split("/"))
    );
}

function addUserToShare() {
    var user = document.getElementById("adduser").value;
    if (!checkUserAddable(user)) {
        alert("Cannot add user. Already shared or user not exists.");
        return;
    }

    var sharedUsers = tmpFile["shared"].split("/");
    sharedUser.push(user);
    tmpFile["shared"] = sharedUser.join("/");

    updateChipContainer();
}

function updateChipContainer() {
    var chipContainer = document.getElementById("chip-container");
    chipContainer.innerHTML = "";

    sharedUser = tmpFile["shared"].split("/");
    for (var j = 0; j < sharedUser.length; ++j) {
        chipContainer.innerHTML += '<div class="chip">';
        chipContainer.innerHTML += sharedUser[j];
        chipContainer.innerHTML += "</div>";
    }
}

function closeShareDialogue(share) {
    var dialoguePage = document.getElementById("share-dialogue-page");
    dialoguePage.classList.remove("full");
    dialoguePage.classList.add("hide");

    if (share) {
        var params = new URLSearchParams();
        params.append("name", tmpFile["name"]);
        params.append("fileSize", tmpFile["fileSize"]);
        params.append("timeStamp", tmpFile["timeStamp"]);
        params.append("md5", tmpFile["md5"]);
        params.append("owner", tmpFile["owner"]);
        params.append("shared", tmpFile["shared"]);

        axios.post("/api/shareFile", params);

        for (var i = 0; i < fileList.length; ++i) {
            if (fileList[i]["name"] == tmpFile["name"]) {
                fileList[i] = tmpFile;
                break;
            }
        }
    }
}

function onLoad() {
    getFileList("/");
    setInterval(getReportMessage, 1000);
    setInterval(function () {
        getFileList("");
    }, 4000);
    setInterval(getUserList, 4000);
}
