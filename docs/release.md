# Release AAB (GitHub Actions)

Workflow: `.github/workflows/android-release.yml`

Dispara com **tag `v*`** (ex.: `v1.0.14`) ou manualmente em **Actions → Release AAB**.

## O que o workflow faz

1. Valida secrets de assinatura
2. Atualiza `version.properties` (tag → nome da tag; manual → incrementa patch)
3. Sincroniza `versionCode` com a Play Store, se a conta de serviço estiver configurada
4. Opcionalmente roda `verifyCi` (lint, detekt, testes, cobertura)
5. Gera AAB release assinado
6. Publica artefatos no GitHub Actions
7. Commita o bump de versão em `main`
8. Envia para faixa **internal** da Play (`completed` por padrão)

## Secrets GitHub (Settings → Secrets and variables → Actions)

| Secret | Obrigatório | Descrição |
|--------|-------------|-----------|
| `ANDROID_KEYSTORE_BASE64` | Sim | Keystore upload (`.jks`) em Base64 |
| `ANDROID_STORE_PASSWORD` | Sim | Senha do keystore |
| `ANDROID_KEY_ALIAS` | Sim | Alias da chave |
| `ANDROID_KEY_PASSWORD` | Sim | Senha da chave |
| `PLAY_STORE_SERVICE_ACCOUNT_JSON` | Não* | JSON da conta de serviço Google Play |

\* Sem este secret o AAB ainda é gerado; o upload para a Play é ignorado.

## Variables (opcional)

| Variable | Default | Descrição |
|----------|---------|-----------|
| `PLAY_RELEASE_STATUS` | `completed` | Use `draft` se o app ainda estiver em rascunho no Play Console |

## Disparo manual

- **run_verify**: roda `./gradlew verifyCi` antes do bundle (default: `true`)
- **skip_play_upload**: gera AAB sem enviar à Play (default: `false`)

## Whats new (Play Store)

Edite `distribution/whatsnew/pt-BR/whatsnew` antes de cada release.

## Tag release

```bash
git tag v1.0.14
git push origin v1.0.14
```

O workflow usa o sufixo da tag como `versionName` e grava em `version.properties`.
