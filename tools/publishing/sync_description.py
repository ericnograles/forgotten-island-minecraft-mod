"""Sync the player guide using CurseForge's undocumented project-update endpoint.

Enable automatic runs only after verifying the saved description in the dashboard.
The documented Upload API does not guarantee this endpoint.
"""
import json
import os
from pathlib import Path
import urllib.error
import urllib.request
import uuid


class NoRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        return None  # Never forward the author token to another destination.


def main():
    project = os.environ["CURSEFORGE_PROJECT_ID"]
    token = os.environ["CURSEFORGE_TOKEN"]
    if not project.isdigit() or not token:
        raise ValueError("Configure CURSEFORGE_PROJECT_ID and CURSEFORGE_TOKEN")
    description = Path("docs/curseforge/description.html").read_text()
    if len(description.strip()) < 100:
        raise ValueError("Refusing an empty or unexpectedly short player guide")
    boundary = uuid.uuid4().hex
    metadata = json.dumps({"description": description, "descriptionType": "html"})
    body = (f'--{boundary}\r\nContent-Disposition: form-data; name="metadata"\r\n\r\n'
            f'{metadata}\r\n--{boundary}--\r\n').encode()
    request = urllib.request.Request(
        f"https://minecraft.curseforge.com/api/projects/{project}/update-project",
        data=body,
        headers={"X-Api-Token": token, "Content-Type": f"multipart/form-data; boundary={boundary}"},
        method="POST")
    try:
        with urllib.request.build_opener(NoRedirect()).open(request, timeout=45) as response:
            print(f"Description update accepted (HTTP {response.status}); verify the dashboard after initial setup.")
    except urllib.error.HTTPError as error:
        # Do not echo remote response bodies, which may contain credentials or HTML.
        raise SystemExit(f"Description update returned HTTP {error.code}. Keep automatic description sync disabled and use the dashboard editor.") from None


if __name__ == "__main__":
    main()
