# Flyspeed

Boost your flight speed up to `20x` with a clean in-game UI, Hold/Toggle activation, and safe-by-default survival support.

## Highlights
- Flight speed multiplier from `1x` to `20x`
- Works instantly while flying, restores normal speed automatically
- Activation mode: `Hold`, `Toggle`, or `Always`
- Optional survival flight support (disabled by default)
- Built-in safety warning before enabling survival boost
- HUD indicator when boost is active
- Full keybind support in `Controls` under the `Flyspeed` category

## Controls
- `Open Flyspeed Settings`: `F8` (default, rebindable)
- `Toggle Survival Flight Boost`: unbound by default (rebindable)

![Flyspeed Settings GUI](https://cdn.modrinth.com/data/cached_images/1103c5ae5ed848fe2f2523e73c492ef468e42b77.png)

## How To Use
1. Start flying in creative mode (or survival flight if enabled in settings).
2. Activate boost with your selected mode:
   Hold mode: hold sprint.
   Toggle mode: press sprint once to turn boost on, press again to turn it off.
   Always mode: boost is active any time you are flying.
3. Open settings with `F8` to adjust multiplier, mode, and toggles.

## Server Policy (Opt-In + Limit)
Flyspeed uses a server policy handshake for multiplayer:
- If a server has Flyspeed installed, it can allow/disable boost and set a max multiplier.

Server owners can edit `config/flyspeed-server.json`:

```json
{
  "allowMovementBoost": true,
  "maxMultiplier": 20.0
}
```

- Set `"allowMovementBoost": false` to disable boost entirely.
- Set `"maxMultiplier"` to cap client boost (range `1.0` to `20.0`).
- Restart server after editing.
