---
id: contributing
title: Contributing
sidebar_label: Contributing
---

${{include($"${resources}/header.md")}}

## Pull Request Checklist

- Branch from the master branch and, if needed, rebase to the current master
  branch before submitting your pull request. If it doesn't merge cleanly with
  master you may be asked to rebase your changes.

- Commits should be as small as possible, while ensuring that each commit is
  correct independently (i.e., each commit should compile and pass tests).

- Don't put sub-module updates in your pull request unless they are to landed
  commits.

- If your patch is not getting reviewed or you need a specific person to review
  it, you can @-reply a reviewer asking for a review in the pull request or a
  comment.

- Add tests relevant to the fixed bug or new feature.


## How to contribute
You can contribute in a myriad of ways:

* submitting issues on [github issues corner](https://github.com/thevpc/nuts/issues) and adding any information you judge important for the maintainers.
  please mark them with 'bug' label. `nuts` should make best effort to work on any environment. So if you encounter any malfunctioning, please contribute with submitting the issue. We are actually unable to test on all environments, so you really are our best hope!
* submitting a feature request again on [github issues corner](https://github.com/thevpc/nuts/issues)
  please detail your idea and mark it with 'enhancement' label.
* working on existing issues. The issues are marked with labels. The priority is given always to the current version milestone (example 0.8.3).
  The complexity of the issue is estimated with the `T-shirt sizing` approach: `size-xxs` is the simplest, `size-m` is medium sized and `size-xxl` is the more complex one.
  Complexity is relative to both required time to do the task and experience on ```nuts``` project to do the work. So please start with smallest (simplest) issues.
* working on media and UX by submitting enhancements/replacements of existing website/icons/themes, etc...
* writing in press about nuts
