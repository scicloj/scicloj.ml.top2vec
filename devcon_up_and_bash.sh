#/bin/sh  
docker exec -it -u vscode -w "/workspaces/$(basename $(pwd))" $(devcontainer up --workspace-folder . | jq -r .containerId) bash
