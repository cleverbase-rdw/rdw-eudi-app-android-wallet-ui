# HOWTO setup the android.yml action

## Useful commands when setting up this action:
```bash
gh variable set FIREBASE_APP_DISTRIBUTION_GROUPS --body "FIREBASE_APP_DISTRIBUTION_GROUPS"
gh secret set FIREBASE_APP_ID --body "get this from firebase"
gh secret set GOOGLE_SERVICES_FILE_CONTENT_BASE64 < app/google-services.json.base64
gh secret set CREDENTIAL_FILE_CONTENT_BASE64 < google-console-cloud-key.json.base64
```

# Run pipeline locally with [act](https://nektosact.com/usage/index.html)
Install act on mac os:
  brew install act

## Prevent accidental commits of sensitive files:
```bash
mkdir -p .git/info/
echo ".env" >> .git/info/exclude
echo ".secrets" >> .git/info/exclude
echo app/google-services.json >> .git/info/exclude
touch .secrets # add 3x secrets with your fave text editor
touch .env # add 1x var with your fave text editor
```

## Confirm that git does not track changes in .env and .secrets
  git status

## Now run act:
  act push

# HOWTO Troubleshoot act
* No space left on device -> docker image prune -a -f && docker system prune -a -f
* Failure to setup on mac os -> act --container-architecture linux/amd64 push
* no space left on device -> docker system prune --all --force --volumes
