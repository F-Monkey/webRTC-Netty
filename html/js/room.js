var body = document.getElementById("body");
let userId = document.getElementById("userId").value;

let createRoomBtn;
let joinRoomDiv;

addCreateRoomInteractAria();
addJoinRoomInteractAria();


function addCreateRoomInteractAria(){
    createRoomBtn = document.createElement("input");
    createRoomBtn.type = "button";
    createRoomBtn.value = "createRoom";
    createRoomBtn.id = "createRoom";
    createRoomBtn.addEventListener("click", function (){
        let createRoom = UserCmd_CreateRoom.create({userId:userId,hasAudio:false,hasVideo:false});
        let content = UserCmd_CreateRoom.encode(createRoom).finish();
        sendMsg(createCmdBuffer(CREATE_ROOM, content));
        cmd_list.push(CREATE_ROOM);
    });
    body.appendChild(createRoomBtn);
}

function addJoinRoomInteractAria() {
    let roomIdInputSign = document.createElement("span");
    roomIdInputSign.innerHTML = "输入房间号: ";
    let roomIdInput = document.createElement("input");
    roomIdInput.type = "text";
    let joinRoomBtn = document.createElement("input");
    joinRoomBtn.type = "button";
    joinRoomBtn.value = "join Room";
    joinRoomBtn.addEventListener("click", function() {
        let roomId = roomIdInput.value;
        if(roomId == 'undefined' || roomId == ""){
            alert("please add roomId");
            return;
        }
        let enterRoom = UserCmd_EnterRoom.create({userId:userId, roomId:roomId});
        let content = UserCmd_EnterRoom.encode(enterRoom).finish();
        sendMsg(createCmdBuffer(ENTER_ROOM, content));
        cmd_list.push(ENTER_ROOM);
    });
    joinRoomDiv = document.createElement("div");
    joinRoomDiv.appendChild(roomIdInputSign);
    joinRoomDiv.appendChild(roomIdInput);
    joinRoomDiv.appendChild(joinRoomBtn);
    body.appendChild(joinRoomDiv);
}

function roomSpan(roomId) {
    let span_div = document.createElement("div");
    let sign_span = document.createElement("span");
    sign_span.innerHTML = "房间号:";
    let span = document.createElement("span");
    span.innerHTML = roomId;
    span.id = "roomId";
    span_div.appendChild(sign_span);
    span_div.appendChild(span);
    return span_div;
}

message_callback_dict[CREATE_ROOM] = function(content){
    let createRoomResult = UserCmd_CreateRoomResult.decode(content);
    let roomId = createRoomResult.roomId;
    createRoomBtn.remove(createRoomBtn.selectedIndex);
    joinRoomDiv.remove(joinRoomDiv.selectedIndex);
    body.appendChild(roomSpan(roomId));
}

message_callback_dict[ENTER_ROOM] = function(content) {
    let joinRoomResult = UserCmd_EnterRoomResult.decode(content);
    let roomId = joinRoomResult.roomId;
    let roomData = joinRoomResult.roomData;
    console.info(roomData);
    createRoomBtn.remove(createRoomBtn.selectedIndex);
    joinRoomDiv.remove(joinRoomDiv.selectedIndex);
    body.append(roomSpan(roomId));
}

message_callback_dict[KICK_OFF] = function(content) {
    let kickOffResult = UserCmd_KickOffCharacterResult.decode(content);
    let roomData = kickOffResult.roomData;
    // you has bean kick off
    if (kickOffResult.kickOffCharacterId == userId) {
        console.info();
    }
    console.info(roomData);
}


