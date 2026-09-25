/* ================= TAB SWITCH ================= */
function openTab(tabId, btn) {
    document.querySelectorAll('.tab').forEach(tab =>
        tab.classList.remove('active')
    );

    document.querySelectorAll('.tabs button').forEach(b =>
        b.classList.remove('active-btn')
    );

    document.getElementById(tabId).classList.add('active');
    btn.classList.add('active-btn');
}

/* ================= IMAGE ================= */
async function encodeImage() {
    const file = document.getElementById("imageEncodeFile").files[0];
    const msg  = document.getElementById("imageMsg").value;
    const pwd  = document.getElementById("imageEncodePassword").value;

    if (!file) {
        alert("Please select an image to encode");
        return;
    }

    const formData = new FormData();
    formData.append("file", file);
    formData.append("message", msg);
    formData.append("password", pwd);

    const res = await fetch("/api/image/encode", {
        method: "POST",
        body: formData
    });

    downloadFile(await res.blob(), "encoded-image.png");
}

async function decodeImage() {
    const file = document.getElementById("imageDecodeFile").files[0];
    const pwd  = document.getElementById("imageDecodePassword").value;

    if (!file) {
        alert("Please select encoded PNG image");
        return;
    }

    const formData = new FormData();
    formData.append("file", file);
    formData.append("password", pwd);

    const res = await fetch("/api/image/decode", {
        method: "POST",
        body: formData
    });

    const txt = await res.text();
    const out = document.getElementById("imageOutput");
    if (res.status === 403) {
        out.innerText = "❌ Wrong password. Access denied.";
        out.style.color = "red";
    } else {
        out.innerText = txt;
        out.style.color = "";
    }
}

/* ================= AUDIO ================= */
async function encodeAudio() {
    const file = document.getElementById("audioEncodeFile").files[0];
    const msg  = document.getElementById("audioMsg").value;
    const pwd  = document.getElementById("audioEncodePassword").value;

    if (!file) {
        alert("Please select an audio file");
        return;
    }

    const formData = new FormData();
    formData.append("file", file);
    formData.append("message", msg);
    formData.append("password", pwd);

    const res = await fetch("/api/audio/encode", {
        method: "POST",
        body: formData
    });

    downloadFile(await res.blob(), "encoded-audio.wav");
}

async function decodeAudio() {
    const file = document.getElementById("audioDecodeFile").files[0];
    const pwd  = document.getElementById("audioDecodePassword").value;

    if (!file) {
        alert("Please select encoded audio file");
        return;
    }

    const formData = new FormData();
    formData.append("file", file);
    formData.append("password", pwd);

    const res = await fetch("/api/audio/decode", {
        method: "POST",
        body: formData
    });

    const txt = await res.text();
    const out = document.getElementById("audioOutput");
    if (res.status === 403) {
        out.innerText = "❌ Wrong password. Access denied.";
        out.style.color = "red";
    } else {
        out.innerText = txt;
        out.style.color = "";
    }
}

/* ================= VIDEO ================= */
async function encodeVideo() {
    const file = document.getElementById("videoEncodeFile").files[0];
    const msg  = document.getElementById("videoMsg").value;
    const pwd  = document.getElementById("videoEncodePassword").value;

    if (!file) {
        alert("Please select a video file");
        return;
    }

    const formData = new FormData();
    formData.append("file", file);
    formData.append("message", msg);
    formData.append("password", pwd);

    const res = await fetch("/api/video/encode", {
        method: "POST",
        body: formData
    });

    downloadFile(await res.blob(), "encoded-video.mp4");
}

async function decodeVideo() {
    const file = document.getElementById("videoDecodeFile").files[0];
    const pwd  = document.getElementById("videoDecodePassword").value;

    if (!file) {
        alert("Please select encoded video file");
        return;
    }

    const formData = new FormData();
    formData.append("file", file);
    formData.append("password", pwd);

    const res = await fetch("/api/video/decode", {
        method: "POST",
        body: formData
    });

    const txt = await res.text();
    const out = document.getElementById("videoOutput");
    if (res.status === 403) {
        out.innerText = "❌ Wrong password. Access denied.";
        out.style.color = "red";
    } else {
        out.innerText = txt;
        out.style.color = "";
    }
}

/* ================= DOWNLOAD HELPER ================= */
function downloadFile(blob, filename) {
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = filename;
    a.click();
    URL.revokeObjectURL(url);
    
    
    
    
    
}