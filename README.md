# Suzaku UI framework

## Motto

> Suzaku helps developers create beautiful, functional and efficient user interfaces for mobile and web platforms.
> It's easy, fun and safe to use and lets developers work purely in Scala.

## Status

Suzaku is currently in *experimental* stage, going through a lot of changes and improvements. It's not quite ready for
building applications but contributors are most welcome!

## Designed features

- Native support for mobile platforms: web, Android and iOS
- Strict separation of UI and application logic 
- Application logic is fully cross-platform
- Designed for multi-core devices
- UI runs at full 60fps and guarantees smooth animations and transitions
- UI components designed for touch + pointer interaction
- Internationalization and localization support, including CJK and RTL scripts
- Customizable layouts and themes
- Unidirectional programming model
- Functional UI definition
- Full-stack shared-source client/server development
- Less is more, restrictions are beneficial and clarity over complexity

## Background and rationale

Developing applications with graphical user interfaces is hard. Making them pretty, consistent and functional is even 
harder. Having all that in a cross-platform environment is close to impossible. Until Suzaku :)

The current frameworks for cross-platform (or even plain web) app development tend to be either based on JavaScript, or
not support all the three major mobile platforms. This is fine for simple applications but as your requirements and
complexity grow, you'll want a more solid language and a flexible framework to get the job done.

Suzaku was born out of the frustration of using JavaScript web frameworks in Scala.js. Even though Scala.js takes a lot of
the web development pain away, it still suffers from the use of JavaScript libraries underneath as they are not designed
to be used in strongly typed languages like Scala. Most of the libraries are also not compatible with each other and the
result is often an unholy alliance of Scala, jQuery, jQuery plugins, React framework and some support components. Some
of these frameworks, however, have good ideas and architectures (for example React) that would work well in a pure Scala
environment.

Suzaku was designed for mobile from the very beginning. Originally just for mobile web apps, but later on it became
apparent that the design would work very well in a true cross-platform environment, too. In mobile you have a serious
problem with performance. The CPUs are weak, but they almost always have multiple cores. Typical web (and native!) apps
do not take advantage of this, but run everything in a single thread. Reason is simple: multithreading is hard
(especially on web) and the frameworks do not provide much (if any) support for it. Thus the first concept of Suzaku was
established:

> Suzaku takes advantage of multiple cores for high performance and responsive UX

In web development you don't get threads, you get _web workers_. These are more like separate processes as they do not
share memory with the main (UI) thread. In Suzaku your application code runs in a separate web worker while just the UI
code runs in the main thread. This keeps the UI responsive even when the app is doing something time consuming and fully
utilizes multiple cores for better efficiency.

However, web workers are isolated and can communicate with each other and the main thread via messages only. To overcome
this challenge, the design must be asynchronous and based solely on messages. What this means in practice is that there
has to be a common _protocol_ between the application and the UI and it has to be serialized to cross the chasm. Now,
this is nothing new, X Window System has had it since 1984! And it is the second concept of Suzaku:

> All communication between the UI and the application is performed using a message protocol in binary

Why binary and not the more typical JSON? Because binary serialization is much more efficient size and speed wise. And
we happened to have a nice serialization library for it (BooPickle).

Having that protocol has the nice side effect that it isolates the application from the actual implementation of the UI,
and therefore opens up avenues towards true cross-platform development. When the UI is just a standard protocol, it
doesn't matter if the implementation is HTML, Android Material UI or iOS Cocoa Touch. They are (effectively) all the
same from the application's point of view.

Now that Suzaku turned into a cross-platform framework, we need to add some abstractions. The communication between threads
is no longer limited to web workers, but could be real threads under Android or iOS (or even remote!), so we need a
library to facilitate that. This is what Arteria was built for. It's a library for establishing virtual protocol
channels over a packet based connection. The protocols are type safe, extensible and automatically serialized (using
BooPickle).

Another benefit of a protocol based UI is that it allows easy testing. You can simple replace a real UI implementation
with a mocked one to create exactly the test situations you need without involving any platform specific code
whatsoever. Testing user interaction scenarios is typically very tedious and hard work, but now it's reduced to defining
a message exchange sequence. Of course you still have to test the actual UI, too, but application interaction logic can
be tested without the real UI.

> Suzaku makes testing application interaction logic easy

Having the groundwork laid for Suzaku, it's time to move onto the details.

In Suzaku the UI is defined using a hierarchy of _widgets_. Each widget class has two parts: one residing on the
application side (backend) and another on the UI side (frontend). These communicate using a protocol built on top of
Arteria. The UI structure is defined in the application code, but it doesn't directly define the layout and other
parameters of the actual UI. This is in contrast to systems like React Native, Nativescript or Xamarin, where the UI
layout is explicitly defined. The UI structure itself is defined in a functional way, much like in React, by a `render`
function returning the widget structure.

... to be continued ...