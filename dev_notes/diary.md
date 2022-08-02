## Humble Beginnings

### Sunday, July 7th 2022 6:48PM PST

---

This first entry marks the beginning of my development journey in creating _Chatter_, a simple N-way messaging service. For the first major chunk of development, we will exclusively focus on getting the service to work for just 2 people messaging back and forth (i.e., Alice and Bob). The plan is to support many people in the same room, though focusing on supporting N-way communication right off the bat could very well lead us astray. We must keep scalability in mind while focusing on progression in the form of baby steps.

Today, we got a few PlantUML sequence diagrams done for:

-   application startup.
-   two users, Alice and Bob, starting and joining a chat session, respectively.

Next time, we will flesh out a 3rd (and hopefully final!) sequence diagram to illustrate the sending of messages back and forth between Alice and Bob.

---

## Design Phase Nearing Its End

### Saturday, July 16th 2022 7:19PM PST

---

Picking up from last time... I found myself struggling to articulate all the pieces of the 3rd (and final) UML diagram into the PlantUML format, so instead, I shifted my focus to the workflow and class diagrams, both of which I was more or less able to finish today.

Both of these documents were completed with pencil and loose leaf, as I felt this allowed me to get my thoughts down on paper in a more steadfast, natural manner. Photos of these documents will be uploaded at a later time. Bear in mind, too, that these documents are intentionally incomplete and will likely be modified as the development process continues.

Next time, I plan to put a cap on the design phase. The remaining design artifacts that require completion are:

-   Task Breakdown -- breaking the development phase down into digestible, logical, easy-to-understand chunks that will then be used to build off of one another.
-   Final Sequence Diagram -- Illustrating the flow of information in a chat session between Alice and Bob. Will probably be done on loose leaf as well.
-   May or may not sketch out an abstract system architecture, illustrating the relationships between the various components of the networked system. If I don't do this tomorrow, then it will probably be done at some point down the line.

Working through the task breakdown should provide a decent picture on which classes would be logical to build out first in the initial portion of the development phase.

#### _Room for Improvement_

I would like to allocate more frequent time blocks to the development of this project. In fact, this is something I need to do for myself in order to satisfy my inner critic. Understandably, I may only get to spend an hour or two during weekdays, but weekends I should be able to allocate somewhere in the ballpark of 4-8 hours each day.

I trust that as I move forward in this development cycle, I will pick up speed and momentum. It will be crucial, however, to develop logical test-driven chunks in order to minimize mistakes and headaches, shortening time-to-market. :)

That's it for now!

---

## Task Breakdown Finished

### Sunday, July 17th 2022 3:51PM PST

---

Title says it all. Finished the task breakdown, including a satisfying amount of detail, in about 2 hours! Going through this forced me to think about and walk through the interactions that will require implementation. I noticed especially a few instances where I uncovered important steps and details that I hadn't previously considered, so this is good. Less surprises down the road. It will also provide a really nice roadmap for development, taking a lot of the guesswork out of "what to build next" sort of thing as the project progresses in logical chunks.

I'll be taking a quick break to hit the gym, then I'll be back for a bit more work before I retire for the evening. I should be able to knock out the final sequence diagram tonight, and I'll see what else I can do with the time remaining before I get ready for bed. :)

---

## Github minutiae

### Tuesday, July 19th 2022 7:23PM PST

Github setup is done with SSH key authentication taken care of. Took me about 40 minutes. Bit of a pain, but I'm glad I got it done.

Upcoming task will be to re-familiarize myself with test-driven development and related styles, in an effort to develop a general approach for myself in building out this project in a successful, time-efficient manner.

---

## Yesterday's Progress

### Sunday, July 24th 12:50PM PST

Yesterday was a productive day! It was also our first day of writing code working on Chatter. Fun stuff.

I spent the first 2ish hours developing the starting Window and Panel for the Chatter app GUI, simultaneously re-learning bits and pieces of GUI programming as I went along. Javanotes by David Eck has been a godsend during this, as well as StackOverflow (both of these will be godsends throughout the context of this entire project, let's be honest here).

**Status**:

Elements of WelcomePanel and MainWindow are more or less all included and laid out in a satisfactory way via BoxLayout.

**To Do Next Here**:

-   Input checking of the text that is passed through the alias submission TextField.
-   Passing of the alias text off to another Thread-based entity that has yet to be determined. This entity is likely to be the newly instantiated _ChatUser_; we will see if can fit this within the context of clean design without making things to difficult for ourselves.

I took a quick break to read a book, eat, go for a walk, then I was back at it for another hour and a half.

During the 2nd burst, I started working on the Registry and its associated RequestHandler. After getting a base of network setup code done, I came across _my first significant conundrum_ so far. I will describe the problem here.

The Registry class emulates that of a singleton. There is only one Registry. It contains static fields, such as _userCount_.

**Registry.java** is a server program, thus it has defined within it a **main()** subroutine. In **main()**, it loops while accepting connections; upon accepting, the resultant socket is passed off to a _RequestHandler_, a class that extends _java.lang.Thread_; my conundrum was to figure out how to define the RequestHandler & Registry classes with respect to one another while keeping the compiler happy:

-   If RH is defined within the Registry class (similar to how event handlers are often defined in their related containing class contexts), I would receive an error when trying to instantiate a new RequestHandler: \*\*"No enclosing instance of type Registry is accessible. Must qualify the allocation with an enclosing instance of type Registry."
-   If RH is defined in a separate file, I end up with circular imports, which is typically indicative of poor code design.

Given that circular imports are undesirable (which they are), then I figured that there must be a slight flaw in my approach to defining either the Registry or the RH classes.

**Solution:**
Define **RequestHandler.java** to be _static_. In doing so, the static nested class can be conveniently packaged along with outer class, resulting in great readability. In this case, only the outer class's static variables (which will be practically all of them) will be accessible to the inner static class. This approach also prioritizes encapsulation and eliminates the need to allocate heap or stack memory for instances of the RequestHandler class. [Reference Material here.](https://www.baeldung.com/java-static)

I was pretty excited when I figured this out for myself. Walking around Walmart later after the fact, I realized that it is little wins like these (i.e., jumps in understanding) that really have been the fuel to my fire for programming in the past, a fire that I reluctantly admit has been dimmed for quite some time. No more though. That flame has been reignited, and it will continue to pick up flame and momentum as I move forward in developing Chatter and further projects! :D

**Learning Moments**:

-   Reflecting on my time yesterday, I probably spent, in each of my two development bursts, a solid 30 minutes of non-development time. I was either responding to messages, fuming over Amazon's delivery service, or pondering over other tasks unrelated to code development. **Moving forward**, I must come up with a stronger sense of focus, perhaps bolstered by a strategy or system, that really encourages **Pure Focus**, as doing so fully envelops the brain into pure bliss of _the Flow State_, where things come together naturally and beautifully.

**To Do Next!!**

-   Finish the Registry side of the things for the first ChatUser-Registry interaction, the purpose of which is assigning a new ChatUser their UID. This part is practically done.
-   Create the **ChatUser.java** class and articulate it's connecting to and communicating with the Registry.

---

## Articulating Initial Net Comms

### Sunday, July 24th 3:15PM PST

Another good day today. So far, I haven't gotten quite as much
done as I would typically be happy with, but perhaps that is just my mind playing tricks on me.

I am at a point in the code where, essentially, the code itself is _slim_, so to speak, but the thought and articulation that goes into making the code work is slightly substantial. Nothing absurd though. This is to be expected in the context of thread-based network programming. I chose to work with Java for good reason, and one of those reasons is to keep this part of the project as simple and as seamless as possible, particularly knowing that I have the most experience with network programming in the context of Java,
C++ probably being the runner-up, and I had no intention of starting off with a C++ project after being cold in programming for quite some time.

All things considered, I am feeling good.

**Status**:

-   Registry side of User setup is more or less done!
-   The foundation of the **ChatUser.java** class has been defined.
-   Wrote code to fetch the desired user alias from **JTextField** solely for KeyEvents generated by the _Return_ key.
-   Started the foundation of _UserSetupThread_, a thread-based helper whose responsibility it will be to communicate with the Registry to fetch a fresh UID and instantiate the null _ChatUser_ reference that was passed through to **LoginPanel.java**.

It feels a little strange passing the ChatUser variable through to LoginPanel, though for now, it seems like a valid strategy for accomplishing what I am going for. Sure, it breaks encapsulation a little bit, but the fact of the matter is, we need a thread-based class that, upon receiving the key press of "Return" in the textfield, fetches the alias and UID, initializing the ChatUser.

Given that the ChatUser's home seems rightfully placed within the scope of **ChatterApp.java**, it only seems right to pass it through to LoginPanel so it can be initialized. Another option would define a Runnable or thread-based class within another class (like **ChatterApp.java**) or in its own file, respectively, and pass _that_ reference in to **LoginPanel.java**, keeping ChatUser in its rightful scope.

Essentially, the decision comes down to determining the most natural and least intrusive way that we break encapsulation in order to fetch the textfield alias, such that we can use it in tandem with the Registry-provided UID to instantiate the ChatUser. There are probably multiple correct answers here.

Determining what we do here will be our task upon returning to work after the gym!!

Roughly 2.5 hours of development time have been logged so far today.

---

## ChatUser "log in" successful!

### Tuesday, July 26th 8:14PM PST

---

Very excited. After some troubleshooting in a few separate places, I **successfully managed** to initialize the ChatUser's UID & alias via multi-threaded networking. As basic as the interaction is that I have concocted, making it come together and work in a matter of two hours is a huge accomplishment, _especially_ considering how long it's been since I have made **any** attempt at networked or threaded code, let alone both of them together in unison!!

I'll describe the interaction here:

-   Registry socket is started on port 8000, waiting for connections.
-   UserChat object is instantiated (but not yet initialized) within the _ChatterApp_ thread of execution. A user chat lock is also constructed and waited on.
-   These two objects are passed to the UserSetupThread via LoginPanel's KeyEventHandler, where the UST worker is constructed and fired up.
-   UST socket connects to the Registry, which fires up a RequestHandler to deal with UST's request.
-   UST passes along the UserSetup protocol message (outlined in **Constants.Java**) along with ChatUser's desired alias; Registry's RequestHandler notes the alias and sends back the appropriate UID for said user.
-   UST initializes the ChatUser reference, after which it closes both input/output streams as well as its socket to the Registry. Finally, it notifies on the user chat lock, declaring ChatUser initialization.
-   ChatterApp's thread wakes up and is able to successfully declare ChatUser's alias, which was received over "the network" and initialized by a separate worker thread!!

Pretty pumped about this. It is now bed time. Early wake up as per usual.

---

## Another Great Day: Part A of Chatter is done.

### Wednesday July 27th, 4:02PM PST

---

Another solid day of code development. Today was more of a focus on UI stuff.

Here's what got done:

-   user alias validation. Includes a descriptive warning message popup that appears, disappearing after 4 seconds.
-   re-worked _MainWindow_'s content layout design & implementation. It now features a JPanel _cardStack_ as its content pane, which is laid out using CardLayout. Taking this approach will allow us to easily flip between panels as we move through the app.
-   implemented **ChoicePanel.java** and aligned the two featured buttons in an appropriate fashion using invisible _javax.swing.Box_ rigid areas to space them out nicely.
-   added the transition code for **MainWindow.java** to switch from **LoginPanel.java** to **ChoicePanel.java**, following the successful initialization of _ChatUser_ as well as a 2.5 second delay; if we have time at the end of this project's development, we could add in a "Loading" animation panel between the two as a less abrupt transition.

At this point in our development, _we have officially finished **Stage A** in our 4-stage development Storyline!_ This is exciting news. We are now onto **Stage B**.

**To Do Next**:

-   add functionality to the "Create a Room" button in **ChoicePanel.java**. This will require us to implement the _SessionThread_ class, as well as some kind of popup UI window that will be our chat room window.

Once we have finished with successfully creating and joining the room as a User, we will then move to implementing "Join Room", which will require us to test the application with **two users**. Things just got a little spicier.

Today's development session was just over two hours.

---

## Two Steps Forward, One Step Back...

### Sunday July 31st, 2:54PM PST

---

#### **Vulnerability ahead...**

This weekend has been a weird one for me, particularly yesterday.

I suppose as someone who is always aspiring toward the tendency of productivity, it comes as a bit of a hit to the mental state when things simply aren't clicking as well as they could or should be. Yesterday was one of those days.

I came in with the idea that I would smash through a bunch of progress with Chatter, only to sit down and barely be able to keep my eyes open, even feeling dizzy and overall just terribly out-of-place.

While there is something to be said about "pushing through adversity" and to get going when the going gets tough, it is quite difficult to even do this when the mind decides to momentarily stop working.

Part of this likely could have been due to the fact that I didn't go in with a clear idea of what I was going to work on, and so when I went to take a look at the pseudocode I had laid out for myself during the week for this weekend's development, I was instantly overwhelmed and became tired even just thinking about what I had to do next.

This is a good lesson for me. In the realm of coding, it is very easy to get overwhelmed when looking at the big picture too long. One should look at the big picture just long enough to get an idea of the full scope of things. Look too long, and become overwhelmed.

Rather, after getting a good idea of said big picture, it then makes sense to zoom in to the "next steps" of where we are headed. That is what we will do now.

**Next Step(s)!**

-   Add functionality to the _Create A Room_ button in **ChoicePanel.java**. This will likely take several development sessions, as the actions being performed are numerous.
-   The first thing to do is program the conversation between a _RoomSetupThread_ and the Registry. This will be straightforward. Programming the Registry's response to this, however, will be non-trivial. The Registry will need to spawn a _SessionThread_.
-   The function of this SessionThread will be to coordinate the communication between all parties in a given chat session. To get this to work fluidly, we will need to have a _ReaderThread_ and a _WriterThread_ for **every single participant** in a given chat session!!
-   As overwhelming as this can be to think about, it makes sense. Articulating this notion in code will be the focus today. How far we get today is really a matter of focus, sheer will and some chance.
-   _ALSO_: Bear in mind that I will be aiming to design and implement the _ReaderThread_ and _WriterThread_ classes in such a way that they can be used on the side of _ChatUsers_ as well. Lots of stuff to think about, easy to get overwhelmed. However, I will remain calm and focused.

I will check in later today before signing off with a progress report. Ciao.

---

## Today's Progress.

### Sunday July 31st, 5:04PM PST

---

We got in a solid 2 hours of focused programming today. I hope to get a larger sum of hours put in tomorrow, as I have the day off of work. 4+ hours would be ideal.

**What got done**:

-   Implemented _UserSetupWorker.java_, a class dedicated to initializing the chat session setup for a given user when "Create A Room" is clicked in _ChoicePanel.java_.
-   Designated a space in _Registry.java_ for chat room setup code to be placed, but have yet to implement this yet.
-   Implemented _OutputWorker.java_ and _InputWorker.java_, the reader/writer thread-based worker classes that will end up playing a **major** role in bringing the functionality of Chatter together.

**To Do Next!**

-   Now that we have _OutputWorker_ and _InputWorker_ done, we are in the position to start building _SessionCoordinator.java_. Before we get into this next time, however, we will want to put in a bit of time thinking about how we would like to articulate the setup of _OutputWorkers_ and _InputWorkers_ for each given chat user that is added to a chat session. This shouldn't be hard, though to rush through the planning of this without putting any thought into it would be silly.
-   Put code in place for _RoomSetupWorker_ to be fired off when "Create a Room" is clicked in _ChoicePanel_.
-   Implement _SessionCoordinator_ setup code in _Registry_ response for a chat room creation request.
-   Set up the start of the conversation between the intended _ChatUser_ host and the _SessionCoordinator_.

That's it for now. The rest of today will be spent at the gym, and depending on how I feel tonight, I will either relax and watch a movie, or I may finish writing my blog post that I started yesterday.

---

## Another Difficult Day

### Monday August 1st, 8:40PM PST

---

Everyone loves to talk about the good days, but what about the bad ones?

I had a bunch of things I wanted to get done today. I had intentions of being superbly productive. I suppose my brain had other plans. I sat down earlier to get some coding done and was absolutely dreading it. So instead, I watched some YouTube scammer payback videos for a hour or so while eating some food and then I went to the gym.

It can be easy to pretend that we are perfect and never miss the mark, when indeed this couldn't be further from the truth. We all have our off-days and off-moments.

When I came back from a torturous leg session (I ensured it was torturous as I was a little upset with myself, and so I used the workout as a bit of a punishment), I was feeling a heck of a lot better about myself. I had a cold shower, ate some food, folded my laundry while listening to David Goggins, and I knew at that point in time that there would be no way I would possibly go to bed without getting at least 90 minutes of coding done in a Pure Focus state. So that is precisely what I did, and I'm extremely proud of myself because of it. The days where we don't want to work, but still find a way to push ourselves to get some amount of work done are truly the most rewarding, even if the production for that day isn't quite as high as we would have liked it to be. Just being able to show up on the rough days and still put in a concerted effort is often times more than enough.

**What Got Done**:

-   **TimeStampGenerator.java** is complete and probably won't require modification.
-   Substantial progress made on **SessionCoordinator.java**.
-   Modified code in _InputWorker_, _OutputWorker_, and _Registry_ to fit what seems to be the most reasonable layout of the _SessionCoordinator_ implementation scheme, for now at least.

**To Do Next**:

-   Add code to support the connecting of _ChatUser_ to the _SessionCoordinator_.
-   Look to implement next the functionality of the "Join Room" in **ChoicePanel.java**. This will take a little bit of careful thought and planning, but with "Create a Room" mostly out of the way, this one should be relatively simpler.
