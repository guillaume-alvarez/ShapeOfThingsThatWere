# GitHub Branch Protection Setup

To ensure PRs cannot be merged unless all checks pass, configure the following branch protection rules in GitHub:

## Required Steps (via GitHub UI)

1. Go to: **Settings → Branches → Branch protection rules → Add rule**
2. Branch name pattern: `master`
3. Enable these options:
   - **Require a pull request before merging**
   - **Require approvals** (set to 1 or more)
   - **Dismiss stale PR approvals when new commits are pushed**
   - **Require review from Code Owners** (if CODEOWNERS file exists)
   - **Require status checks to pass before merging**
     - Required checks (must match workflow job names exactly):
       - `Build & Test`
       - `Smoke Test`
   - **Require branches to be up to date before merging**
   - **Require conversation resolution before merging**
   - **Require linear history** (optional but recommended)
   - **Include administrators** (to enforce on admins too)

## Required Status Checks

The CI workflow defines these job names that must pass:
- `Build & Test` (job: `build`)
- `Smoke Test` (job: `smoke-test`)

These must be added in the "Require status checks to pass before merging" section.

## Configure via GitHub CLI (optional)

If you have `gh` CLI installed and authenticated:

```bash
# Enable branch protection with required checks
gh api repos/guillaume-alvarez/ShapeOfThingsThatWere/branches/master/protection \
  --method PUT \
  --field required_status_checks='{"strict":true,"contexts":["Build & Test","Smoke Test"]}' \
  --field enforce_admins=true \
  --field required_pull_request_reviews='{"required_approving_review_count":1,"dismiss_stale_reviews":true,"require_code_owner_reviews":false}' \
  --field restrictions=null \
  --field required_linear_history=true \
  --field allow_force_pushes=false \
  --field allow_deletions=false \
  --field required_conversation_resolution=true
```

## Verify Configuration

After setup, create a test PR to verify:
1. Both "Build & Test" and "Smoke Test" checks run
2. Merge button is disabled until both pass
3. Merge button remains disabled if either fails