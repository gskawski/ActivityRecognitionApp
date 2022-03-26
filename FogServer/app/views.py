
from app import app
from flask import render_template, request, redirect
import os

@app.route('/')
def hello():
    # return 'Glenn Skawski Gesture Videos'
    return render_template("public/index.html")

app.config["VIDEO_UPLOADS"] = "/Users/glennskawski/ASUProjects/535/app/app/static/videos/SKAWSKI_GLENN_Gestures"

@app.route('/upload-video', methods=["GET","POST"])
def upload_video():
    if request.method == "POST":
        if request.files:
            # request.content_length returns the content length in bytes
            content_length = request.content_length
            print(f"Content length: {content_length}")

            # content_type
            content_type = request.content_type
            print(f"Content type: {content_type}")

            # request.mimetype returns the mimetype of the request
            mimetype = request.mimetype
            print(mimetype)

            # Get an ImmutableMultiDict of the files
            file = request.files
            print(file)

            video = request.files["videofile"]
            video.save(os.path.join(app.config["VIDEO_UPLOADS"], video.filename))
            print("video saved")
            return redirect(request.url)
    return render_template("public/upload_video.html")
