let PackageGroup;
let Package;
let Cmd;

protobuf.load("/proto/Cmd.proto", function (err, root) {
    if (err) throw err;
    Package = root.lookup("Package");
    PackageGroup = root.lookup("PackageGroup");
    Cmd = root.lookup("Cmd");
});

let UserCmd_Login;
let UserCmd_LoginResult;
let UserCmd_Logout;
let UserCmd_LogoutResult;
let UserCmd_CreateRoom;
let UserCmd_CreateRoomResult;
let UserCmd_CloseRoom;
let UserCmd_CloseRoomResult;
let UserCmd_KickOffCharacter;
let UserCmd_KickOffCharacterResult;
let UserCmd_EnterRoom;
let UserCmd_EnterRoomResult;
let UserCmd_StartChatting;
let UserCmd_StartChattingResult;

protobuf.load("/proto/UserCmd.proto",function(error, root){
    if(error) throw error;
     UserCmd_Login = root.lookup("Login");
     UserCmd_LoginResult = root.lookup("LoginResult");
     UserCmd_Logout = root.lookup("Logout");
     UserCmd_LogoutResult = root.lookup("LogoutResult");
     UserCmd_CreateRoom = root.lookup("CreateRoom");
     UserCmd_CreateRoomResult = root.lookup("CreateRoomResult");
     UserCmd_CloseRoom = root.lookup("CloseRoom");
     UserCmd_CloseRoomResult = root.lookup("CloseRoomResult");
     UserCmd_KickOffCharacter = root.lookup("KickOffCharacter");
     UserCmd_KickOffCharacterResult = root.lookup("KickOffCharacterResult");
     UserCmd_EnterRoom = root.lookup("EnterRoom");
     UserCmd_EnterRoomResult = root.lookup("EnterRoomResult");
     UserCmd_StartChatting = root.lookup("StartChatting");
     UserCmd_StartChattingResult = root.lookup("StartChattingResult");
});

function createCmdBuffer(cmdType, content){
    cmd = {cmdType:cmdType, content:content};
    return  Cmd.encode(cmd).finish();
}

let Entity_Character;
let Entity_RoomData;

protobuf.load("/proto/Entity.proto", function(error, root){
    if(error) throw error;
    Entity_Character = root.lookup("Character");
    Entity_RoomData = root.lookup("RoomData");
});