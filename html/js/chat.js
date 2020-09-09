function createScreenShare(){
    navigator.mediaDevices.getDisplayMedia(constraints).then(stream =>{
        localVideo.srcObject = stream;
        shareStream(stream);
    }).catch(err =>{
        console.error(err);
    });
}

function createCameraChat(){
    navigator.mediaDevices.getUserMedia(constraints).then(stream =>{
        localVideo.srcObject = stream;
        shareStream(stream);
    }).catch(err => {
        console.error(err);
    });
}