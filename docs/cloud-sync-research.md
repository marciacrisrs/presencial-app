# Pesquisa: sincronização na nuvem (Issue #8)

> Comparativo Google Drive, OneDrive e Dropbox para backup/sincronização do Presencial sem servidor próprio.

## Contexto

O Presencial já exporta/importa backup JSON v3 (`BackupManager`) via Storage Access Framework (SAF). A issue #8 pede sincronização automática entre dispositivos usando apenas APIs client-side dos provedores.

**Dados sincronizados:** configurações, check-ins, resumos mensais, endereços de trabalho (JSON ~dezenas a centenas de KB).

---

## Comparativo

| Critério | Google Drive | OneDrive | Dropbox |
|----------|--------------|----------|---------|
| **SDK Android** | `play-services-auth` + Drive API v3 | MSAL + Microsoft Graph | Dropbox SDK v2 |
| **Maturidade mobile** | Excelente (referência Android) | Boa | Boa |
| **OAuth / UX login** | Conta Google nativa no SO | Conta Microsoft | App Dropbox ou OAuth |
| **Pasta oculta do app** | `appDataFolder` (não visível ao usuário) | OneDrive App Folder | App folder API |
| **Sync incremental** | Upload/download de arquivo único; metadados `modifiedTime` | Delta queries (Graph) | Revisions + cursor |
| **Offline** | Fila manual (WorkManager) | Idem | Idem |
| **Conflitos** | Last-write-wins (adequado a 1 backup) | Idem | Idem |
| **Quotas API** | ~12.000 req/dia/projeto (grátis) | Graph throttling | Tier dev gratuito |
| **Custo usuário** | 15 GB grátis (conta Google) | 5 GB grátis | 2 GB grátis |
| **Criptografia cliente** | Possível antes do upload | Possível | Possível |
| **iOS futuro** | GoogleSignIn iOS + Drive REST | MSAL iOS | Dropbox SDK iOS |
| **Complexidade** | Média | Alta (Graph + tenant) | Média-alta |
| **Manutenção** | Baixa (ecossistema Google Android) | Média | Média |

### Pontos fortes / fracos

**Google Drive**
- (+) Integração nativa Android; `DRIVE_APPDATA` evita expor arquivo ao usuário
- (+) Documentação extensa; exemplos oficiais
- (+) Maioria dos usuários Android já tem conta Google
- (−) Requer projeto Google Cloud + OAuth (SHA-1 do keystore)

**OneDrive**
- (+) Microsoft Graph unificado (Outlook, Teams)
- (+) Delta sync robusto para apps maiores
- (−) Fluxo OAuth mais pesado; MSAL extra
- (−) Menos natural em Android puro

**Dropbox**
- (+) API de arquivos simples; app folder dedicado
- (−) SDK menos usado em apps Android BR
- (−) Quota free menor; pede app Dropbox em alguns fluxos

### APIs relevantes

| Provedor | Autenticação | Upload | Download | Listar backup |
|----------|--------------|--------|----------|---------------|
| Google Drive | Google Sign-In + `DriveScopes.DRIVE_APPDATA` | `files.create` / `files.update` | `files.get` | `files.list` (`spaces=appDataFolder`) |
| OneDrive | MSAL → Graph `/me/drive/special/approot` | `PUT /children/{name}/content` | `GET /content` | `GET /children` |
| Dropbox | OAuth2 → `/2/files/upload` | `files/upload` (app folder) | `files/download` | `files/list_folder` |

---

## Recomendação: **Google Drive**

1. **Android-first:** app atual é Android; Drive é o caminho de menor atrito.
2. **`appDataFolder`:** backup fica na pasta oculta do app — alinhado à privacidade (usuário não vê JSON solto no Drive).
3. **Reuso:** o payload continua sendo o JSON do `BackupManager`; só muda o transporte.
4. **Custo zero** para o volume do Presencial (1 arquivo, sync sob demanda ou periódico).
5. **OneDrive/Dropbox:** manter exportação manual via SAF (já funciona com qualquer nuvem) e adicionar provedores extras em fases futuras via interface `CloudSyncProvider`.

---

## Plano de integração

### Fase 1 — Protótipo (este PR) ✅

| Tarefa | Estimativa | Risco |
|--------|------------|-------|
| Documento de pesquisa | 0,5 d | Baixo |
| `CloudSyncProvider` + `GoogleDriveCloudSyncProvider` | 1 d | Médio (OAuth GCP) |
| `CloudSyncRepository` + bytes no `BackupManager` | 0,5 d | Baixo |
| UI Configurações (login, sync, restore) | 0,5 d | Baixo |
| Testes unitários | 0,5 d | Baixo |
| **Total Fase 1** | **~3 d** | |

### Fase 2 — Produção

| Tarefa | Estimativa |
|--------|------------|
| WorkManager sync periódico + toggle “sync automático” | 1 d |
| Incluir `absences` no backup v4 | 0,5 d |
| Criptografia AES-GCM opcional antes do upload | 1 d |
| Validar `version` na importação + migração | 0,5 d |
| Atualizar política de privacidade | 0,25 d |

### Fase 3 — Multi-provedor (opcional)

| Tarefa | Estimativa |
|--------|------------|
| OneDrive via Microsoft Graph | 2 d |
| Dropbox SDK | 1,5 d |
| Seletor de provedor na UI | 0,5 d |

### Migração / riscos

- **OAuth:** desenvolvedores precisam criar projeto GCP e registrar SHA-1 (debug + release). Ver [README — Google Drive](../README.md#google-drive-sync).
- **Conflito:** estratégia inicial last-write-wins; backup único `presencial_backup.json`.
- **Privacidade:** texto atualizado — dados só vão à nuvem se o usuário conectar e sincronizar.
- **Android Auto Backup:** continua independente; JSON na nuvem é opt-in explícito.

---

## Protótipo implementado

- Pacote `com.presencial.app.data.sync`
- Seletor de provedor (Google Drive / OneDrive / Dropbox)
- Conexão de pasta via **Storage Access Framework** (permissão persistente)
- Upload/download de `presencial_backup.json` reutilizando `BackupManager`
- UI em **Configurações → Sincronização na nuvem**
- Exportação manual JSON preservada

**Fase 2 (futuro):** OAuth nativo Google Drive (`appDataFolder`) para sync sem escolher pasta — ver plano acima.
