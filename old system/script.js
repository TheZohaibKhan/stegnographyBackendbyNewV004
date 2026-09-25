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

    if (!file) {
        alert("Please select an image to encode");
        return;
    }

    const formData = new FormData();
    formData.append("file", file);
    formData.append("message", msg);

    const res = await fetch("/api/image/encode", {
        method: "POST",
        body: formData
    });

    downloadFile(await res.blob(), "encoded-image.png");
}

async function decodeImage() {
    const file = document.getElementById("imageDecodeFile").files[0];

    if (!file) {
        alert("Please select encoded PNG image");
        return;
    }

    const formData = new FormData();
    formData.append("file", file);

    const res = await fetch("/api/image/decode", {
        method: "POST",
        body: formData
    });

    document.getElementById("imageOutput").innerText =
        await res.text();
}

/* ================= AUDIO ================= */
async function encodeAudio() {
    const file = document.getElementById("audioEncodeFile").files[0];
    const msg  = document.getElementById("audioMsg").value;

    if (!file) {
        alert("Please select an audio file");
        return;
    }

    const formData = new FormData();
    formData.append("file", file);
    formData.append("message", msg);

    const res = await fetch("/api/audio/encode", {
        method: "POST",
        body: formData
    });

    downloadFile(await res.blob(), "encoded-audio.wav");
}

async function decodeAudio() {
    const file = document.getElementById("audioDecodeFile").files[0];

    if (!file) {
        alert("Please select encoded audio file");
        return;
    }

    const formData = new FormData();
    formData.append("file", file);

    const res = await fetch("/api/audio/decode", {
        method: "POST",
        body: formData
    });

    document.getElementById("audioOutput").innerText =
        await res.text();
}

/* ================= VIDEO ================= */
async function encodeVideo() {
    const file = document.getElementById("videoEncodeFile").files[0];
    const msg  = document.getElementById("videoMsg").value;

    if (!file) {
        alert("Please select a video file");
        return;
    }

    const formData = new FormData();
    formData.append("file", file);
    formData.append("message", msg);

    const res = await fetch("/api/video/encode", {
        method: "POST",
        body: formData
    });

    downloadFile(await res.blob(), "encoded-video.mp4");
}

async function decodeVideo() {
    const file = document.getElementById("videoDecodeFile").files[0];

    if (!file) {
        alert("Please select encoded video file");
        return;
    }

    const formData = new FormData();
    formData.append("file", file);

    const res = await fetch("/api/video/decode", {
        method: "POST",
        body: formData
    });

    document.getElementById("videoOutput").innerText =
        await res.text();
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
