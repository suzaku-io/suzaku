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
* [Background and motivation](#background-and-motivation) - tells you why Suzaku was created
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

You can now try out the demo app in your browser at URL
[http://localhost:12345/webdemo/index.html](http://localhost:12345/webdemo/index.html), served by the wonderful
[Workbench plugin](https://github.com/lihaoyi/workbench).

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

This section covers the architecture and design of Suzaku by going through the codebase in a (hopefully) meaningful order. We
walk through the various classes, traits and objects that make up Suzaku to give you an overview how things work together and
how applications are built using Suzaku.

### Project structure

Suzaku is a monorepo consisting of several interdependent projects. Here is a list of projects and what they are for:

| Project                     | Description                                            |
|:----------------------------|:-------------------------------------------------------|
| `core`                      | Platform independent core of Suzaku.                   |
| `base-widgets`              | Platform independent definitions of base widgets.      |
| `platform/web/core`         | Web specific implementation of the core.               |
| `platform/web/base-widgets` | Web specific implementation of the base widgets.       |
| `webdemo`                   | A simple application demonstrating the user of Suzaku. |

### UI and Application separation

In Suzaku the user interface implementation and your application code are running on different threads to benefit from
multi-core CPUs commonly used in all modern devices.

![multicore](image/multicore.png)

What this means in practice is that your application actually starts in the UI (main) thread and needs to instantiate the
application thread separately. To simplify the required application startup code, Suzaku provides
[`UIBase`](../../core/shared/src/main/scala/suzaku/app/UIBase.scala) and
[`AppBase`](../../core/shared/src/main/scala/suzaku/app/AppBase.scala) helper classes.

The UI and the App communicate via a [`Transport`](../../core/shared/src/main/scala/suzaku/platform/Transport.scala) that
sends messages between the two threads using a platform specific implementation.

![transport](image/transport.png)

The application developer must provide the transport and pass it to the constructors of `UiBase` and `AppBase`. For example
in a web application UI thread you would create an instance of
[`WebWorkerTransport`](../../platform/web/core/src/main/scala/suzaku/platform/web/WebWorkerTransport.scala)
(`WorkerClientTransport` to be exact) to utilize Web Worker message passing mechanism as the transport.

```scala
var transport: WebWorkerTransport = _

@JSExport
def entry(): Unit = {
  // create the worker to run our application in
  val worker = new Worker("worker.js")
  // create the transport
  transport = new WorkerClientTransport(worker)
  // listen to messages from worker
  worker.onmessage = onMessage _
  val ui = new WebDemoUI(transport)
}

def onMessage(msg: dom.MessageEvent) = {
  msg.data match {
    case buffer: ArrayBuffer =>
      transport.receive(buffer)
    case _ => // ignore other messages
  }
}
```

Similarly in the application thread you would create a `WorkerTransport` and pass it to your application base class.

```scala
var transport: WebWorkerTransport = _

@JSExport
def entry(): Unit = {
  // create transport
  transport = new WorkerTransport(self)
  // receive WebWorker messages
  self.onmessage = onMessage _
  // create the actual application
  val app = new WebDemoApp(transport)
}

def onMessage(msg: dom.MessageEvent) = {
  msg.data match {
    case buffer: ArrayBuffer =>
      transport.receive(buffer)
    case _ => // ignore other messages
  }
}
```

On both sides the setup is very similar, creating a transport on top of Web Worker messages and attaching listeners to pass
those messages to the transport implementation. This is all you need to do in the application entry point to get Suzaku
running.

Your application specific `WebDemoUI` and `WebDemoApp` are also rather trivial:

```scala
class WebDemoUI(transport: Transport) extends UIBase(transport) {
  override val platform = WebPlatform

  override protected def main(): Unit = {
    suzaku.platform.web.widget.registerWidgets(widgetRenderer)
  }
}

class WebDemoApp(transport: Transport) extends AppBase(transport) {
  override protected def main(): Unit = {
    val comp = TestComp("Testing")
    uiManager.render(comp)
  }
}
```

On the UI side you need to define what [`Platform`](../../core/shared/src/main/scala/suzaku/platform/Platform.scala) you are
using and register all the widgets you intend to use. On the application side you just need to render the root component of
your user interface to get things started. This is much like in React where you'd call `ReactDOM.render`.

### Declarative User Interface

The user interface in Suzaku is defined _declaratively_ by calling the
[`UIManager`](../../core/shared/src/main/scala/suzaku/ui/UIManager.scala)`.render` method. You provide a _root_ component,
which in turn contains a _tree_ of other components and widgets. But these components and widgets are not real instances, but
_blueprints_ that declare what kind of component/widget should be instantiated. For example a simple login view component
might contain a `render` method that returns a tree of blueprints (simplified for the example):

```scala
def render(state: State) = {
  Layout.Vertical()(
    Label("Email"),
    Input.Text(state.email),
    Label("Password"),
    Input.Password(state.password),
    Button("Cancel"),
    Button("Login")
  )
}
```

We can illustrate this blueprint tree like this:

![blueprint](image/blueprint.png)

The call to, for example, `Button("Login")` will return an instance of `ButtonBlueprint` which extends
[`WidgetBlueprint`](../../core/shared/src/main/scala/suzaku/ui/WidgetBlueprint.scala) and is defined as:

```scala
case class ButtonBlueprint private[Button] (label: String, onClick: Option[() => Unit] = None) extends WidgetBlueprint {
  type P     = ButtonProtocol.type
  type Proxy = ButtonProxy
  type This  = ButtonBlueprint

  override def createProxy(viewId: Int, uiChannel: UIChannel) = new ButtonProxy(this)(viewId, uiChannel)
}
```

This blueprint is not the actual widget, it just declares how to create the widget. The blueprint contains _all_ the
information necessary to create an instance of the actual `Button` widget. If you are familiar with React, the blueprint is
quite similar to what component _props_ are in React, except in Suzaku the blueprint applies to both higher order
_components_ and _widgets_ alike.

### Widgets, blueprints, proxies and protocols

So what exactly is a _widget_ in Suzaku? The concrete widget resides safely in the UI thread whereas it's represented by a
[`WidgetProxy`](../../core/shared/src/main/scala/suzaku/ui/WidgetProxy.scala) on the App thread, which communicates with the
widget using a `Protocol`. The _proxy_ and the _widget_ are instantiated by Suzaku according to the data given in the
[`Blueprint`](../../core/shared/src/main/scala/suzaku/ui/Blueprint.scala). Sounds rather complicated (and it is!), but for
the user of Suzaku it's all hidden behind the blueprint.

![button-widget](image/button-widget.png)

But for someone wanting to create their own widgets, it's crucial to understand how these things work under the hood. Let's
walk through the creation of a `Button` widget and see what happens behind the scenes.

When a user calls `Button("Login")` they actually call `Button.apply(label: String)` method in the `Button` object. This
method constructs an instance of `ButtonBlueprint` which is passed into the blueprint tree maintained by Suzaku. If this is
the first time Suzaku sees the blueprint in that location of the tree, it will create a proxy by calling `createProxy` in the
blueprint, passing an internal `viewId` and the Arteria channel used for UI communication. The `ButtonProxy` extends
[`WidgetProxy`](../../core/shared/src/main/scala/suzaku/ui/WidgetProxy.scala) which provides the basic implementation for
proxies.

```scala
class ButtonProxy private[Button] (bd: ButtonBlueprint)(viewId: Int, uiChannel: UIChannel)
    extends WidgetProxy(ButtonProtocol, bd, viewId, uiChannel)
```

When the `WidgetProxy` is constructed, it create a unique channel for communicating with the actual widget in the UI thread.
The widget is identified by the class name of the blueprint. The `initView` method returns the initial state for the widget,
which in the case of `Button` contains just the `label` from the blueprint.

```scala
protected val channel =
  uiChannel.createChannel(protocol)(this, initView, CreateWidget(blueprint.getClass.getName, viewId))
```

At this point things are pretty much done for the App thread, so let's take a look what happens on the UI thread. The
[`WidgetRenderer`](../../core/shared/src/main/scala/suzaku/ui/WidgetRenderer.scala) gets a callback to _materialize_ a new
channel for the widget. Suzaku will look up an appropriate _builder_ for the widget (`DOMButtonBuilder` in this case) and
calls it to create the actual widget
([`DOMButton`](../../platform/web/base-widgets/src/main/scala/suzaku/platform/web/widget/DOMButton.scala)) passing the
initial state (containing the label).

Within `DOMButton` an _artifact_ is created by instantiating a suitable DOM element:

```scala
val artifact = {
  import scalatags.JsDom.all._
  DOMWidgetArtifact(button(context.label, onclick := onClick _).render)
}
```

The artifact is not attached to the DOM document at this point, it happens as part of updating the _children_ of the parent
widget (`Layout`). Once the `button` element is in the document, it becomes visible to the user and allows interaction.

### Widget messaging

So what happens when a user _clicks_ the button?

As we saw above, the `DOMButton` registers an event handler on the `button` element, listening to click events. When the user
clicks the button it will call this handler, which sends a `Click` message on the channel.

```scala
def onClick(e: dom.MouseEvent): Unit = {
  channel.send(Click)
}
```

The `Click` message is defined in `ButtonProtocol` and is simply a case object. At this point it might be good to take a look
at the implementation of `ButtonProtocol`:

```scala
object ButtonProtocol extends Protocol {

  sealed trait ButtonMessage extends Message

  case class SetLabel(label: String) extends ButtonMessage

  case object Click extends ButtonMessage

  val bmPickler = compositePickler[ButtonMessage]
    .addConcreteType[SetLabel]
    .addConcreteType[Click.type]

  implicit val (messagePickler, witnessMsg) = defineProtocol(bmPickler)

  case class ChannelContext(label: String)

  override val contextPickler = implicitly[Pickler[ChannelContext]]
}
```

A widget protocol is required to define three things:

1. Valid messages for this protocol (and an implicit witness(es) to provide evidence)
2. Initial channel context to be passed when the channel is created
3. Picklers (serializers) for messages and the context

In the case of button the only valid messages are `SetLabel`, which is used to update the button and `Click`, which is used
to indicate that the button was clicked. Note that the channel is always bidirectional, so any message can be sent in either
direction but typically most of the messages travel only in one direction.

When a message is sent on the channel using the `send` method, Suzaku (and Arteria underneath) pass it through the transport
to the other side, where it's routed to the appropriate message handler. This handler is the `process` method in the
`ButtonProxy` class:

```scala
override def process = {
  case Click =>
    blueprint.onClick.foreach(f => f())
  case message =>
    super.process(message)
}
```

In `process` we handle only the `Click` message and call the click handler defined in the blueprint, if any.

### UI changes

Previously we covered what happens on the first `render` call when the `Button` widget gets created, but how are changes in
the UI handled, for example if we change the button text from "Login" to "Logout"? Let's find out!

Because Suzaku is based on declarative UI, we need to render the UI again with the changed button component. Everything else
stays the same, but the last button is now defined as `Button("Logout")`. When the call to `uiManager.render` completes,
Suzaku will compare the cached blueprint tree to the new tree returned by `render`. It will walk the tree and check if

1. widget type has changed
2. widget type is the same, but blueprint has changed

In the latter case it will call `ButtonBlueprint`'s `sameAs` method, which by default calls `equals` and therefore will
notice the change in the label. Next it will pass the new `ButtonBlueprint` to the current `ButtonProxy` instance using its
`update` method, which will check if the label has changed and then send an appropriate message to the widget to update the
label.

```scala
override def update(newBlueprint: ButtonBlueprint) = {
  if (newBlueprint.label != blueprint.label)
    send(SetLabel(newBlueprint.label))
  super.update(newBlueprint)
}
```

This allows widgets to have very fine grained control over how to make updates in the UI. There's no need to perform
expensive virtual DOM comparisons, as the widget knows directly what needs to be updated.

The `DOMButton` receives the `SetLabel` message, and updates the DOM accordingly.

```scala
override def process = {
  case SetLabel(label) =>
    modifyDOM(node => node.replaceChild(textNode(label), node.firstChild))
}
```

Note how the widget is performing a direct DOM manipulation, as it has full control over its own DOM tree. This makes updates
very efficient as the widget can decide the best update strategy.

### Components

If Suzaku provided only widgets, the application would have to render the whole static widget blueprint tree from the root on
every little change. To get more dynamic updates, you need to use _components_. Like widgets, components are also represented
by blueprints, but extending [`ComponentBlueprint`](../../core/shared/src/main/scala/suzaku/ui/ComponentBlueprint.scala). The
`ComponentBlueprint` is even simpler than `WidgetBlueprint`, having only two methods:

```scala
trait ComponentBlueprint extends Blueprint {
  def create(proxy: StateProxy): Component[_, _]

  def sameAs(that: this.type): Boolean = equals(that)
}
```

The `sameAs` method is used to check if the blueprint has changed from the previously rendered just like in widgets, and the
`create` method is used to instantiate the actual [`Component`](../../core/shared/src/main/scala/suzaku/ui/Component.scala).

#### Component lifecycle

A component has a well defined _lifecycle_ that begins by calling the **constructor** of your component class when it's first
mounted. This is followed by a call to `initialState` to get the, you know, initial state of your component. Next up is a
call to `render` to retrieve the contents of the component. Finally `didMount` is called to indicate that your component is
ready.

![lifecycle-construction](image/lifecycle-construction.png)

After the component has been constructed, there are only three things that can happen to it:

1. a blueprint change
2. a state change
3. destruction

A blueprint change means that the component was rendered again by some other component higher in the hieararchy, but with
different parameters, for example the label on a `Button` might have changed. Instead of building a new component, Suzaku
informs the mounted component about the change through `willReceiveBlueprint` providing access to the upcoming blueprint so
that the component can modify its internal state if necessary. This is followed by a call to `shouldUpdate` which is Suzaku's
way of asking "has something really changed or should we just skip render"? If it returns `true` (as it does by default), the
component `render` is called, followed by a call to `didUpdate`.

![lifecycle-blueprint](image/lifecycle-blueprint.png)

For a _state_ change the call sequence is the same, except the `willReceiveBlueprint` is naturally skipped.

![lifecycle-state](image/lifecycle-state.png)

Finally after the component is removed from UI and destroyed, its `didUnmount` method is called. This is a good place to remove
any handlers or callbacks you might have registered in `didMount`.

![lifecycle-unmount](image/lifecycle-unmount.png)

#### Component state

What makes components useful is that they can have internal mutable _state_. This allows components to react to incoming
events like user interaction and modify their state, which leads to re-rendering of the component. But the state of component
is held tight inside Suzaku and the component is only given read-only (not strongly enforced, though) access to it. The
different callback methods liked `render` and `shouldUpdate` are given a reference to the current state, so that component
code may use it. Otherwise it's hidden. Note that this _state_ should only be used to store data that is needed in the render
and all other internal "state" should be kept as variables in the component class. This would include things like handles for
registered listeners that need to be unregistered at unmount time. Suzaku never recreates the component instance, so your
data is safe inside the class.

To make changes in the state, you have to go through `modState(f: State => State)` giving as parameter a function that
performs the desired change in the state. This change may happen asynchronously at a later time, when the Suzaku frameworks
sees fit to actually perform the change (or a list of pending changes). In any case, before any callback method is called,
the state will have been modified. In a typical scenario the state is reprented as a `case class` and modifications to it use
the `copy` method to only partially update the class. For example:

```scala
case class MyState(user: String, password: String)
...
modState(s => s.copy(user = newUser))
```


... to be continued ...

### UI testing

A benefit of a protocol based UI is that it allows easy testing. You can simple replace a real UI implementation with a
mocked one to create exactly the test situations you need without involving any platform specific code whatsoever. Testing
user interaction scenarios is typically very tedious and hard work, but now it's reduced to defining a message exchange
sequence. Of course you still have to test the actual UI, too, but application interaction logic can be tested without the
real UI.

... More coming soon! ...


## Workflow

This is the general workflow for contributing to Suzaku (adapted from
[Scala.js](https://github.com/scala-js/scala-js/blob/master/CONTRIBUTING.md))

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
