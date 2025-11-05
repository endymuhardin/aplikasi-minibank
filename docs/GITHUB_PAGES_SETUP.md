# GitHub Pages Setup Guide

This guide explains how to enable GitHub Pages for automatic documentation deployment.

## Prerequisites

- Repository must be public OR have GitHub Pro/Team/Enterprise plan
- GitHub Actions must be enabled in repository settings
- Push access to the main branch

## Setup Steps

### 1. Enable GitHub Pages

1. Go to your repository on GitHub
2. Click **Settings** tab
3. Scroll down to **Pages** section in the left sidebar
4. Under **Source**, select **GitHub Actions**
5. Click **Save**

### 2. Repository Permissions

The GitHub Actions workflow requires specific permissions. These are already configured in `.github/workflows/maven.yml`:

```yaml
permissions:
  contents: read
  pages: write
  id-token: write
```

### 3. Workflow Triggers

The documentation workflow will automatically run when:
- Code is pushed to the `main` branch
- A pull request is merged to `main`

### 4. Access Your Documentation

After the first successful workflow run:

1. **Public URL**: `https://<your-username>.github.io/<repository-name>/`
2. **Check deployment**: Go to **Settings > Pages** to see the live URL
3. **View builds**: Go to **Actions** tab to monitor workflow progress

## What Gets Deployed

The automated workflow generates and deploys:

- **index.html**: Landing page with modern styling
- **panduan-approval-workflow.html**: Approval workflow user manual (V2.0)
- **README.html**: Documentation index
- **screenshots/**: All captured screenshots from Playwright tests
- **videos/**: All captured videos from Playwright tests
- **Original markdown files**: For direct access if needed

> **Note**: Version 2.0 introduces approval workflow. All new customers require Branch Manager approval.

## Troubleshooting

### Common Issues

1. **"Pages site is not yet built"**
   - Wait 5-10 minutes after workflow completion
   - Check Actions tab for any workflow failures

2. **404 on GitHub Pages URL**
   - Verify GitHub Pages is enabled in Settings > Pages
   - Ensure workflow completed successfully
   - Check that files were uploaded to pages-build-deployment

3. **Screenshots not displaying**
   - Verify screenshots were generated in the workflow logs
   - Check file paths in the generated HTML
   - Ensure screenshots directory was copied correctly

4. **Workflow fails at Playwright step**
   - Check that all system dependencies are installed
   - Verify Playwright test runs locally first
   - Review workflow logs for specific error messages

### Monitoring Deployments

- **Actions tab**: View workflow execution and logs
- **Settings > Pages**: See deployment history and status
- **Environments**: View github-pages deployment details

## Customization

### Modify Landing Page

Edit the index.html generation section in `.github/workflows/maven.yml` starting at line ~129.

### Add More Documentation

1. Create new Playwright documentation tests (e.g., `ApprovalWorkflowTutorialTest.java`)
2. Create corresponding documentation generator (e.g., `ApprovalWorkflowDocGenerator.java`)
3. Modify workflow to run new tests and generators

### Custom Domain (Optional)

1. Add `CNAME` file to pages directory in workflow
2. Configure custom domain in Settings > Pages
3. Update DNS records as instructed by GitHub

## Security Considerations

- Documentation is publicly accessible via GitHub Pages
- Ensure no sensitive information is captured in screenshots
- Review generated content before deployment
- Consider using private repositories for sensitive projects

## File Structure

```
GitHub Pages Site Structure (Version 2.0):
├── index.html (landing page - redirects to approval workflow guide)
├── panduan-approval-workflow.html (NEW - main user manual)
├── panduan-approval-workflow.md
├── README.html
├── README.md
├── screenshots/
│   ├── 01_halaman_login.png
│   ├── 25_halaman_detail_approval.png
│   └── ... (34 approval workflow screenshots)
└── videos/
    ├── approval_workflow_*.webm
    └── ... (step-by-step video recordings)
```

---

**Generated**: $(date '+%d %B %Y')  
**Documentation System**: Playwright + GitHub Actions + GitHub Pages  
**Target Audience**: DevOps, Documentation Maintainers