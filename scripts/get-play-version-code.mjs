import { google } from 'googleapis';
import { GoogleAuth } from 'google-auth-library';

const packageName = process.argv[2];
if (!packageName) {
  console.error('Usage: node scripts/get-play-version-code.mjs <packageName>');
  process.exit(1);
}

const credentialsJson = process.env.PLAY_STORE_SERVICE_ACCOUNT_JSON;
if (!credentialsJson) {
  console.error('PLAY_STORE_SERVICE_ACCOUNT_JSON is required');
  process.exit(1);
}

const auth = new GoogleAuth({
  credentials: JSON.parse(credentialsJson),
  scopes: ['https://www.googleapis.com/auth/androidpublisher'],
});

const androidPublisher = google.androidpublisher({ version: 'v3', auth: await auth.getClient() });

const edit = await androidPublisher.edits.insert({ packageName });
const editId = edit.data.id;

try {
  const { data } = await androidPublisher.edits.tracks.list({ packageName, editId });
  const versionCodes = (data.tracks ?? [])
    .flatMap((track) => track.releases ?? [])
    .flatMap((release) => release.versionCodes ?? [])
    .map(Number)
    .filter((code) => Number.isFinite(code));

  const maxVersionCode = versionCodes.length > 0 ? Math.max(...versionCodes) : 0;
  process.stdout.write(String(maxVersionCode));
} finally {
  await androidPublisher.edits.delete({ packageName, editId });
}
