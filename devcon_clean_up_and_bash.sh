#/bin/sh  
docker exec -it -u vscode -w "/workspaces/$(basename $(pwd))" $(devcontainer up --workspace-folder . --remove-existing-container | jq -r .containerId) bash
