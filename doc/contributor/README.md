# Welcome, dear contributor

Contributors, those mythical beings who unselfishly sacrifice their time in effort to help out fellow developers, whom they
may have never met, in building high quality software and libraries. You are most welcome. This documentation is for you.

## The Why

Contributors are important. Most open source projects suffer from a low
[bus factor](https://en.wikipedia.org/wiki/Bus_factor), often having only a single maintainer, on whom the whole project then
depends. It is the responsibility of the project maintainer(s) to encourage and help potential contributors to get started
with the project. That is why this documentation exists.

Typical open source library documentation (if it exists in the first place :smirk: ) is intended for the _user_ of the
library, describing how the library is used etc. This is of course fine for the user, but it doesn't help a potential
contributor that much, as they would like to know more about the internals, architecture and design of the library. Unlike
user documentation, this _contributor documentation_ focuses on exactly that, making it easier to get started with your
contributions, whether they are just fixing typos in the documentation, reporting issues, correcting them, writing tests or
improving the overall design. All contributions from all contributors are welcome!

## The Guide

This guide is divided into following sections to help you get around quickly.

* [Getting started](#getting-started) - taking those first baby steps
* [Background and motivation](#Background-and-motivation) - tells you why Suzaku was created
* [Design and Architecture](#design-and-architecture) - the core principles and ideas behind Suzaku
* [Workflow](#workflow) - the actual workflow for contributing
* [Style guide](#style-guide) - keeping up with the latest style
* [Practical tips](#practical-tips) - making your life easier with some practical tips

## Getting started

First of all make sure you have the necessary tools installed. You'll need the following:

* Git ([downloads](https://git-scm.com/downloads) for all platforms)
* Java 8 JDK ([downloads](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) for all
  platforms)
* SBT (setup for [Linux](http://www.scala-sbt.org/0.13/docs/Installing-sbt-on-Linux.html) |
  [Windows](http://www.scala-sbt.org/0.13/docs/Installing-sbt-on-Windows.html) |
  [Mac](http://www.scala-sbt.org/0.13/docs/Installing-sbt-on-Mac.html))

To make contributions in the project you need to _fork_ your own copy of it first. Go to
[https://github.com/suzaku-io/suzaku]() and click the _Fork_ button to do it.

![fork](image/fork.png)

This will create a copy of the current repo state under your own account (for example `ochrons/suzaku`), where you can play
around freely. Next step is to create a local _branch_ where you'll make your changes. In this example we'll be adding
Android support.

![createbranch](image/createbranch.png)

You can also create the branch locally after you have cloned the repo to your own computer, which we'll do next.

Some contributions, like small fixes to documentation, can be done using nothing but the GitHub web UI, but for most cases
you'll need to _clone_ the repository on your own computer by clicking the "Clone or download" button, which will provide you
with an appropriate link text. If you have added your
[SSH key to GitHub](https://help.github.com/articles/connecting-to-github-with-ssh/), use the SSH protocol, otherwise choose
HTTPS.

![clonerepo](image/clonerepo.png)

Next you'll need to perform the actual cloning of the repo using your `git` client, for example on the command line with

```
$ git clone git@github.com:ochrons/suzaku.git
```

This will create a `suzaku` directory under your current working directory and copy the contents of the repo (with all the
branches and other metadata) there.

Finally switch to the branch you created earlier, so you can get started with your modifications.

```text
$ cd suzaku
$ git checkout add-android-support
```

If you didn't create a branch in the GitHub UI, now is the perfect time to do that with

```text
$ cd suzaku
$ git checkout -b add-android-support
```

To make sure everything works correctly, start `sbt` and compile the included example app with

```text
$ sbt
> webDemo/fastOptJS
```

You can now try out the demo app in your browser at URL [http://localhost:12345/webdemo/index.html](), served by the
wonderful [Workbench plugin](https://github.com/lihaoyi/workbench).

Now you're all set to play with the source code, make your modifications and test them locally. Before submitting your
contribution, please read the [Workflow](#workflow) and [Style guide](#style-guide) for more information on how.

## Background and motivation

Developing applications with graphical user interfaces is hard. Making them pretty, consistent and functional is even harder.
Having all that in a cross-platform environment is close to impossible. Until Suzaku :smile:

The current frameworks for cross-platform (or even plain web) app development tend to be either based on JavaScript, or not
support all the three major mobile platforms (web, Android and iOS). This is fine for simple applications but as your
requirements and complexity grow, you'll want a more solid language and a flexible framework to get the job done.

Suzaku was born out of the frustration of using JavaScript web frameworks in Scala.js. Even though Scala.js takes a lot of
the web development pain away, it still suffers from the use of JavaScript libraries underneath as they are not designed to
be used in strongly typed languages like Scala. Most of the libraries are also not compatible with each other and the result
is often an unholy alliance of Scala, jQuery, jQuery plugins, React framework and some support components. Some of these
frameworks, however, have good ideas and architectures (for example [React](https://facebook.github.io/react/) ) that would
work well in a pure Scala environment.

### Designed for mobile first

Suzaku was designed for mobile from the very beginning. Originally just for mobile web apps, but later on it became apparent
that the design would work very well in a true cross-platform environment, too. In mobile you have a serious problem with
performance. The CPUs are weak, but they almost always have multiple cores. Typical web (and native!) apps do not take
advantage of this, but run everything in a single thread. Reason is simple: multithreading is hard (especially on web) and
the frameworks do not provide much (if any) support for it. Thus the first concept of Suzaku was established:

> Suzaku takes advantage of multiple cores for high performance and responsive UX

In web development you don't get threads, you get _web workers_. These are more like separate processes as they do not share
memory with the main (UI) thread. In Suzaku your application code runs in a separate web worker while just the UI code runs
in the main thread. This keeps the UI responsive even when the app is doing something time consuming and fully utilizes
multiple cores for better efficiency.

### UI isolation

However, web workers are isolated and can communicate with each other and the main thread via messages only. To overcome this
challenge, the design must be asynchronous and based solely on messages. What this means in practice is that there has to be
a common _protocol_ between the application and the UI and it has to be serialized to cross the chasm. Now, this is nothing
new, X Window System has had it since 1984! And it is the second concept of Suzaku:

> All communication between the UI and the application is performed using a message protocol in binary

Why binary and not the more typical JSON? Because binary serialization is much more efficient size and speed wise. And we
happened to have a nice serialization library for it ([BooPickle](https://github.com/suzaku-io/boopickle)).

Having that protocol has the nice side effect that it isolates the application from the actual implementation of the UI, and
therefore opens up avenues towards true cross-platform development. When the UI is just a standard protocol, it doesn't
matter if the implementation is HTML, Android Material UI or iOS Cocoa Touch. They are (effectively) all the same from the
application's point of view.

Now that Suzaku turned into a cross-platform framework, we need to add some abstractions. The communication between threads
is no longer limited to web workers, but could be real threads under Android or iOS (or even remote!), so we need a library
to facilitate that. This is what [Arteria](https://github.com/suzaku-io/arteria) was built for. It's a library for
establishing virtual channels over a packet based connection. The protocols are type safe, extensible and automatically
serialized (using BooPickle).

### Friendly API

Although under the hood Suzaku uses some rather advanced techniques, it still needs to be easy and fun to use, while
remaining powerful and extensible at the same time. Suzaku borrows heavily from React (and especially
[React Native](http://facebook.github.io/react-native/)) in the way the UI is constructed. In principle the UI is fully
declarative and mirrors the state of your application. You just tell Suzaku what the user interface should look like _right
now_ and Suzaku makes it happen.

Everything is based on _components_ and _widgets_ making it easy to build complex applications through composition. For most
applications it's quite enough to use the built-in widgets, but Suzaku also allows the application to define their own
widgets with platform specific implementations.

## Design and Architecture

Coming soon!

### UI testing

A benefit of a protocol based UI is that it allows easy testing. You can simple replace a real UI implementation with a
mocked one to create exactly the test situations you need without involving any platform specific code whatsoever. Testing
user interaction scenarios is typically very tedious and hard work, but now it's reduced to defining a message exchange
sequence. Of course you still have to test the actual UI, too, but application interaction logic can be tested without the
real UI.

## Workflow

This is the general workflow for contributing to Suzaku.

1. You should always perform your work in its own Git branch (a "topic branch"). The branch should be given a descriptive
   name that explains its intent.

2. When the feature or fix is completed you should [squash](https://help.github.com/articles/about-pull-request-merges/) your
   commits into a one (per useful change), [rebase](https://help.github.com/articles/about-git-rebase/) it against the
   original branch and open a [Pull Request](https://help.github.com/articles/about-pull-requests/) on GitHub. Squashing
   commits keeps the version history clean and clutter free. Typically your PR should target the `master` branch, but there
   may be other work-in-progress branches that you can also target in your PR. This is especially true if you have originally
   branched from such a WIP branch.

3. The Pull Request should be reviewed by the maintainers. Independent contributors can also participate in the review
   process, and are encouraged to do so. There is also an automated CI build that checks that your PR is compiling ok and
   tests run without errors.

4. After the review, you should resolve issues brought up by the reviewers as needed (amending or adding commits to address
   reviewers' comments), iterating until the reviewers give their thumbs up, the "LGTM" (acronym for "Looks Good To Me").
   While iterating, you can push (and squash) new commits to your topic branch and they will show up in the PR automatically.

5. Once the code has passed review the Pull Request can be merged into the distribution.

### Tests

Tests are nice. They help keep the code in working condition even through refactorings and other changes. When you add new
functionality, please write some relevant tests to cover the new functionality. When changing existing code, make sure the
tests are also changed to reflect this.

### Documentation

All code contributed to the user-facing part of the library should come accompanied with documentation. Pull requests
containing undocumented code will not be accepted.

Code contributed to the internals (ie. code that the users of Suzaku typically do not directly use) should come accompanied
by internal documentation if the code is not self-explanatory, e.g., important design decisions that other maintainers should
know about.

### Creating Commits And Writing Commit Messages

Follow these guidelines when creating public commits and writing commit messages.

#### Prepare meaningful commits

If your work spans multiple local commits (for example; if you do safe point commits while working in a feature branch or
work in a branch for long time doing merges/rebases etc.) then please do not commit it all but rewrite the history by
squashing the commits into **one commit per useful unit of change**, each accompanied by a detailed commit message. For more
info, see the article: [Git Workflow](http://sandofsky.com/blog/git-workflow.html). Additionally, every commit should be able
to be used in isolation - that is, each commit must build and pass all tests.

#### Use `scalafmt`

Run `scalafmt` before making a commit to keep source code in style.

#### First line of the commit message

The first line should be a descriptive sentence about what the commit is doing, written using the imperative style, e.g.,
"Change this.", and should not exceed 70 characters. It should be possible to fully understand what the commit does by just
reading this single line. It is **not ok** to only list the ticket number, type "minor fix" or similar. If the commit has a
corresponding ticket, include a reference to the ticket number, with the format "Fix #xxx: Change that.", as the first line.
Sometimes, there is no better message than "Fix #xxx: Fix that issue.", which is redundant. In that case, and assuming that
it aptly and concisely summarizes the commit in a single line, the commit message should be "Fix #xxx: Title of the ticket.".

#### Body of the commit message

If the commit is a small fix, the first line can be enough. Otherwise, following the single line description should be a
blank line followed by details of the commit, in the form of free text, or bulleted list.

## Style guide

Suzaku has a rather flexible styling requirements for contributors so don't fret about it too much. The maintainers will ask
you to fix clear style violations in your contributions or just correct them in the next refactoring/clean-up. All source
code in Suzaku is formatted using [scalafmt](http://scalameta.org/scalafmt/) to maintain coherent style across the project.
What this means is that you don't have to worry too much about the style, but just let `scalafmt` handle it! Before making a
commit, just run `sbt scalafmt` to apply the default styling to the whole codebase.

It is of course much appreciated if you'd spend a few moments looking through the existing code base and try to pick up some
clues about the style being used ;)

## Practical tips

Coming soon!
