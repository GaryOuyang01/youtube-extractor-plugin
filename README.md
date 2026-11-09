# youtube-extractor-plugin

A small Android app with one job: search YouTube and resolve playable stream addresses for another app, over a
bound `Messenger` service with plain JSON text. It uses [NewPipeExtractor](https://github.com/TeamNewPipe/NewPipeExtractor)
and is therefore released under the **GNU General Public License v3.0 or later** (see `LICENSE`).

- Package: `org.aikarasong.plugin.extractor`
- Service action: `org.aikarasong.plugin.extractor.BIND`
- No code is shared with the app that talks to it; the two only exchange JSON text.

## Protocol (version 1)

A request is a `Message` (`what = 1`, `replyTo` set) whose `Bundle` holds the JSON text under the key `json`. The reply
has the same layout.

```
request: {"v":1,"id":n,"op":"ping|searchPage|resolveStream|details|playlist", ...fields}
reply:   {"id":n,"ok":true, ...fields}   or   {"id":n,"ok":false,"error":"text"}
```

| op | fields in | fields out |
|---|---|---|
| `ping` | none | `protocol`, `version` |
| `searchPage` | `query`, `cursor` (optional token) | `videos[]` (`id`, `title`, `channel`, `durationSec`, `thumb`, `views`), `next` (token or null) |
| `resolveStream` | `videoId` | `videoUrl`, `audioUrl` (or null) |
| `details` | `videoId` | `tracks[]` (`lang`, `formats[]` of `ext`, `url`), `description`, `autoLangs[]`, `allLangs[]` |
| `playlist` | `playlistId` | `videos[]` |

Search cursors are opaque tokens kept inside this app (last 50); a token it no longer knows ends the list.

## Build

Requires JDK 17 and the Android SDK (`ANDROID_HOME`, or `sdk.dir` in `local.properties`).

```
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleRelease      # unsigned; sign it with your own key
```

## Notes

- YouTube changes often; when extraction breaks, update the NewPipeExtractor version in `gradle/libs.versions.toml`
  and publish a new release.
- Using YouTube may be subject to its terms of service and to copyright law where you live. This project is provided
  as is, without warranty (see `LICENSE`).
