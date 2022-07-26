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

---

## Decent Progress Today.

### Tuesday, August 2nd 4:11PM PST

---

Title says it all. I allotted myself 90 minutes of development time and I made use of every minute. Only ever looked at my phone to change the song playlist or skip the song. Started off with some basic techno geek coding music, then transitioned to the Halo 3 soundtrack (much better imo).

Music banter aside, I'm happy with what got done today. The progress won't really show for much until the next couple of days, as we are in a bit of a delayed gratification / awkward phase of development where a bunch of work must be done behind the scenes to make ends meet on the application side of things. I also noticed a pretty major design flaw in the way my code was laid out in main(); essentially my code was being executed sequentially, not within a loop. When I considered the option of adding a "Back" button to **ChoicePanel.java**, my design flaw came to light. I addressed this flaw by adding in the enum-based _ApplicationState_ that is used within the context of a while loop. Other than this, we essentially spent all the rest of the time laying the groundwork for ChatUser to be able to start working on its own within its threaded context to communicate directly with the SessionCoordinator.

**What Got Done**:

-   Altered main() execution architecture to switch from strictly sequential-based to a loop + state-based execution style.
-   Added code to various classes to allow for AppState to work congruently across the application.
-   Ironed out kinks across _Registry_, _RoomSetupWorker_, _ChatterApp_, and possibly others that I'm forgetting with the objective of setting up the communication channel between _ChatUser_ and _SessionCoordinator_.

**To Do Next**:

-   Flesh out **ChatWindow.java**. This will likely require a thread-based worker that receives message updates and pushes them onto the screen. More on this later.
-   Implement the thread-based execution of _ChatUser_ in its interaction with _SessionCoordinator_.

Good day.

-   ***

## Getting Right Into It.

### Thursday, August 4th 2:58PM PST

---

Not wasting any time. Current task is to get a minimalist ChatWindow up and running with all the necessary components: a chat feed, a place to type messages, and a participants list. So long as we get the alignment and formatting relatively correct, we will move back to getting _TextUpdateWorker_ built, as well as the putting the infrastructure in place for text updates to be able to be displayed as they come in from the ChatUser's _InputWorker_. Let's get to work.

**What Got Done**:

-   Learned the basics of GridBagLayout.
-   Applied (somewhat unsuccessfully) to apply the principles of said layout manager.

Components are laid out relatively properly. The issue now is to get the components to take up the correct amount of space. The weights, in particular, are somewhat mystical and difficult to deal with. In this sense, the component layout of _ChatWindow_ will take a little more time, hopefully not too much. I will see if I can put in some more work tonight after finishing my cooking for the next week, as well as some gym time, which I probably will. I may end up considerably chopping my sleep tonight just to make progress on this layout manager, as it has truly proven to be an annoyance in the brief 90 minutes that I have been working on it today.

I firmly dislike UI coding, so whenever it presents issues, I like to get them out of the way as fast as possible so more important code can be attended to. That is the plan going forward.

**To Do Next**:

-   Continue working with _GridBagLayout_ for the components within **ChatWindow.java**.

---

## More GUI Stuff

### Friday, August 5th 4:49PM PST

---

Made good progress on _ChatWindow_ GUI formatting. Almost done here. 90 minutes worth of work done today. Might do more tonight.

---

## Today's Agenda

### Saturday, August 6th 1:52 PM PST

---

A good day. **ChatWindow.java** is mostly done. Now, the past AJ would have mindlessly fiddled with it even more to get it absolutely _perfect_, whereas the current AJ is already a touch frustrated that I spent two straight development days working on GUI code. What a world of a difference. It is in this world that I currently reside that I am now transitioning my focus back into the context of threaded workers and ABQs.

So... Now that _ChatWindow_ is more or less done in terms of presentation code, we now have to write code that will link said GUI code into the main functionality of the program _MEANING_... We must write code that will push text typed into the messaging interface by the user, so that when "Enter" is pressed, said text is pushed into every chat user's text feed. Getting the participant list to function is another thing, but should be relatively trivial once the chat feed - text messaging interface is nailed down nicely.

Before we get there, however, we must loop back and spend a brief amount of time on _more GUI code_. Sigh... The reason I say this is because in order for Bob to join Alice's chat room, Bob must click on "Join Room", which should then bring Bob to a screen that shows all the available rooms for Bob to join in a simple listwise fashion. We could call this panel **RoomSelectPanel.java**. This will be the **last** panel that will need to be created for Chatter (thank the Lord!!), and for further consolation, the required layout of the GUI elements will be extremely simple: A list of chat rooms, followed by two buttons at the bottom, "Back" and "Join". The "Join" button should rightfully be greyed out and unpressable _unless_ a room listing is selected, which can be signified by a simple clicking of the listing.

After finishing this final bit of GUI code, our next task will be to iron out the "Create A Room" process for Alice, which if I remember correctly, should be mostly done, at least in terms of the fundamental networking base code that links up a _ChatUser_ with the _SessionCoordinator_. We will look at this in more depth when the time comes. In fact, we will do this first to give ourselves a bit of a break from GUI code, one of the things I am slightly less fond of in the world of programming.

So without further ado, let us get on with development.

**To Do Today**:

-   Dive back in to the development of connecting _ChatUser_ with _SessionCoordinator_.
-   Upon clicking "Create a Room", have a _ChatWindow_ pop up with the correct admin message showing at the top of the chat feed.

### 6:58 PST

Well... I am pretty flippin tired. I want to keep working, but I also know I should go to the gym. I am stuck at a bug that I will explore more probably tomorrow, not tonight. In short, I am getting an UnknownHostException when trying to connect _ChatUser_ to _SessionCoordinator_, even after I have bound SC's ServerSocket to localhost. For whatever reason, it wants to have its IP as 0.0.0.0/0.0.0.0, which seems a little strange, though I believe Registry's ServerSocket is also bound to 0.0.0.0 and didn't have any problems. I have some digging to do, though right now, I am absolutely beat.

Roughly 4 hours of work put in today.

---

## Enormous Progress in a Short Span

### Sunday, August 7th 7:07PM PST

---

So in just a 90-minute session of Pure Focus (i.e., no checking my phone AT ALL), I hammered through and made a ton of progress:

-   Fixed an UnknownHostException bug that had me stuck at the end of yesterday when I was feeling tired and defeated.
-   Fixed a timezone bug (super simple change from "PST" to "Canada/Pacific")
-   And finally, I got myself to the point of _ChatWindow_ displaying the welcome message from _SessionCoordinator_, which is absolutely huge. This means that all the background work I put in to building out **InputWorker.java**, **OutputWorker.java**, and their subclasses is now paying off, as they all seem to get their jobs done quite well, at least for now.
-   Implemented **MyListModel.java**, which extends Java's _DefaultListModel_, which allows me to add text to read-only things like _ChatWindow's_ participant list and chat feed without having to call any refresh or validate functions.
-   Altered _ChatWindow_ to use _JList_ instead of _JTextArea_ for the chat feed and participant list.
-   Writing & fixing other miscellaneous chunks of code along the way.

**All in 90 minutes**. Love to see it.

**To Do Next**:

#### Minor Fixes (easy to do, we'll knock these out right away next time)

-   Perform minor _ChatWindow_ modifications (i.e., make the window taller + wider).
-   Add code to have the host participant's name added to the participant list.

#### Less Minor Stuff

-   Build out **RoomSelectionPanel.java** that will eventually allow Bob to join Alice's room.
-   Ensure Alice's room is being displayed properly within the aforementioned panel's list of rooms.
-   Add functionality to allow Bob to join Alice's room.

We are getting closer and closer to this project seeing its completion. Once Bob and Alice are in the same room, we practically already have all the pieces in place for getting them to successfully send messages to one another. That is, **UserInputWorker.java**, **OutputWorker.java**, and **UserInputHandler.java**. Things are wrapping up, and it makes me excited to think about that, **ESPECIALLY** considering that I have stuck relatively well to my initial timeline.

Speaking of which... The project phases I had initially mapped out for the development of Chatter were a tad misaligned. Phase B really contains most of the work. Phase A was no joke, though Phases C and D are both code cleanup phases more or less. So once we are able to have Bob and Alice successfully sending messages to one another, the project will be about 75% complete. We will then be in the position of adding functionality such as leaving one room to join another, going back to the main menu to change one's alias, and things of that nature. Compared to what I have done thus far, however, these finishing touches should be comparably simple. Exciting stuff knowing that I am coming close to finishing, **not** so I can sit idly to appreciate what I've done, but so I can shift my focus toward other things that demand my attention in the realm of personal branding.

That's it for now. :)

---

## Some Minor Progress

### Tuesday, August 9th 7:58PM PST

---

Didn't get too much done today. Got some work done on **RoomSelectPanel.java**. Brainstormed the layout and started building it out, including the implementation of **RoomSelectTable.java**, a custom _JTable_ class that will be used to display the room info. Only got an hour in tonight. Life preoccupations, though not ideal. I wanted 90 minutes and got 60. Going for a walk with my mother, getting some reading in, and bed by 9ish for a 3am work. 'Tis is life. I will be more disciplined with my time at the gym tomorrow... Got a little carried away talking to people today. It's hard sometimes not having a social life. Oh well, part of the grind. Until next time.

---

## Design on the Fly

### Wednesday, August 10th 3:13PM PST

---

The mission today was to flesh out **RoomListPanel.java**. As I started to pick back up where I had left off, my scrambled
brain jumped around perhaps a little too much, as I started to think about the process of getting the room list data from _Registry_ and jumping to that instead of finishing the task at hand. I let my attention slip today in that regard. In hindsight looking back on today, I should have _certainly_ finished working on **RoomListPanel.java** before jumping over to building out
_RoomsListHander_, the thread-based worker class that I came up with on-the-fly that will be responsible for handling requests pertaining to room lists and room list refreshes.

I am happy, however, that I am able to realize this directly after the fact. In any case, the fact that my brain is slipping in this regard is likely indicative of a need for me to catch up on a sleep _a little_ and maybe shift gears a tad bit. With that said, it may be wise use tomorrow and the next day to take a step back, look at the big picture, and revisit the design side of things while getting a tidbit more shut-eye for the sake of performance.

With that being my intent over the next few days, this will put me in the position to charge into the weekend, full-steam ahead with a more focused idea of what tasks need to be completed and in what order I shall choose to complete them.

Until next time.

---

## Room Select Panel Looking Clean...

### Thursday, August 11th 4:36PM PST

---

Title says it all. Feeling pretty dead. Was hammering pretty good today to get **RoomSelectPanel.java** looking tight. Very happy with how it looks.

_Features/Notes_:

-   Completed the _GridBagLayout_ to a satisfactory degree. Format, layout and spacing are all looking really nice.
-   Row selection and deselection enabled & synchronized with "Join" button (button enables when row is selected and vice versa)
-   Model is verified to be showing data.

Today involved a lot of Googling and solving of a bunch of small tiny issues. It was definitely frustrating at times, particularly in trying to pinpoint the correct words to feed into the search engine that would give me the results I was looking for with all the particular issues I was solving, such as:

-   enabling row selection while disabling cell selection and cell editability
-   getting the table data to actually show up
-   enabling table grid lines
-   adding deselect & select capabilities, such that when either the containing panel (i.e., NOT the table or any buttons), OR a non-row within the table was clicked, any selected row would deselect.
-   pairing the select/deselect capability with enabling on/off of the "Join" button.
-   Getting GridBagLayout to play nicely, particularly with grouping "Refresh" and "Join" next to each other in the bottom-right corner with "Back" alone in the bottom-left.

Overall a fun little coding session. Was initially only going to code for 90 minutes, but I told myself I wouldn't leave the coffee shop until I was satisfied with the panel I was working on. So I stayed an extra hour and got it done. Feelsgoodman.

**Next Time...**

We'll look at getting some real data into the table (i.e., Alice's room data). Depending on how long this takes us with background processes needing to be taken care of, we will shift our focus to programming the entry of Bob into Alice's room.

---

## Background Processes

### Saturday, August 13th 5:52PM PST

---

Our first sprint session of the day has come to a close.

In this ~2hr time span, we have almost completely fleshed out the _RoomsListFetcher_, a **RoomSelectPanel.java** worker that communicates with the Registry to fetch new (as well as refreshed) lists of rooms that are available to be joined. It is coming along very nicely.

The last bit of work needed for RLF to be done is really to wrap up its class methods (which we are 90-95% done with), after which our work will be to synchronize said worker with the various buttons on _RoomSelectPanel_'s interface. This will take little to no time at all.

Once we are done with that, it will be about time to see if we can boot up two separate instances of the Chatter application for Alice and Bob to interact with one another. The goal will be to get Bob into Alice's room, though whether or not we get to that point will depend on how many bugs/issues we encounter along the way and how tricky they are to solve. Only time will tell.

### 8:11PM PST

After some careful consideration and design thoughts, we have a little bit of coordination work and thread communication to be done in order to allow for Bob to join Alice's room. For this to happen, _SessionCoordinator_ must undergo development in order for the class to reach its final form (relatively speaking).

In other words, we are addressing a TODO that was planted weeks ago. We put it off knowing that it would take some thoughtful consideration, as well as some intelligent programming.

The thoughtful consideration is somewhat complete. We have partially established the logical steps required for Bob to join Alice's room:

1. End user clicks "Join" button on _RoomSelectPanel_. This, then, fires up a _JoinRoomWorker_ thread.
2. JRW connects to the Registry, sending along a _Join Room Request_. This request takes the form of "<JoinRequestString> <AliasOfRequestingUser> <NameOfRoomToJoin>\n".
3. Registry RequestHandler sees this, looks up the connection information for the _SessionCoordinator_ for that particular room, responds back with the connection info for that SC, closes the socket with JRW, then notifies the SC.

We stop here to make a careful distinction. In _SessionCoordinator_'s main line of execution (which we will look to flesh out tomorrow), it can only call _wait()_ on one Object at any given moment. Initially, my plan was to assign two distinct responsibilities to _SessionCoordinator_; that is, forwarding newly received messages to other users in the chat room, and incorporating new users to the chat room. This, by nature, is not a good idea by nature of OOP, and will likely overcomplicate my program.

Given my desire for adherence to OOP and KISS (Keep it simple, stupid!), I think it would be wise to pivot my decision-making here, and give SC the **sole** responsibility of bringing new users into the chat room. This, of course, happens after SC has created the chat streaming avenues for the host itself, which technically can be considered bringing a new user into the chat room (adherence... heheh).

With that in mind, I think it would be wise to create another thread worker whose sole responsibility is to forward newly received messages from one _InputWorker_ to all the _OutputWorkers_, aside from the one OW that is correlated with the IW to avoid redundancy. We will do this tomorrow.

If and when we finish this task tomorrow, we will simply return back to the aforementioned ordered list of things that need to be in place for Bob to join Alice. _JoinRoomWorker_ will need to be fleshed out at some point. Perhaps that is the next chunk we will look flesh out.

Another ~2hrs of development time were put in here.

Until next time.

---

## Chipping Away

### Sunday, August 14th 6:09PM PST

---

Bit of a lesser day today in terms of progress. I could certainly push the envelope and work later, but I am beginning to think that I really need to start taking my sleep more seriously. Sure, maybe I will get a little less work done today, but in doing so, getting 7 hours of sleep instead of 4, 5 or 6 will, in turn, set me up for success for tomorrow, and the next day, and the day after that.

Despite only spending 2 hours writing code, today was still quite productive:

-   Implemented **Worker.java**, a desirable abstract superclass to have that ultimately eliminates code redundancy for common code across all worker thread classes. This wasn't in the agenda for today, but I realized that it was a desirable thing to get done for worker ID stuff, as well as the common turnOn()/turnOff() methods that should really exist for all workers.
-   Implemented **BroadcastWorker.java**, a worker class that is responsible for performing the message forwarding task mentioned yesterday. The reason I chose to call it a _broadcast_ worker is because really, that's what it is doing: taking a message from one user, and sending that message to all the rest of the users in that list of users. To my knowledge, that is the essence of message broadcasting.
-   Modified existing worker classes to subclass _Worker_.
-   Modified **SessionCoordinator.java** to utilize _BroadcastWorker_. Also compartmentalized some code into an **initializeUser()** method, minimizing redundancy between the procedure of initializing the room host and users joining the room in general.

**To Do Next:**

-   Flesh out **JoinRoomWorker.java**. This will be a bit of a task, but shouldn't be too tough. We have outlined a very clear procedure on paper of what needs to happen within this worker's line of execution.
-   Test it out and see if it works.

That will probably be enough for tomorrow. If we can get Bob to join Alice by tomorrow, that would be an enormous success. Because from that point, we are really only a hop, skip and a jump from getting two users to talk to one another, and with the architecture I have in place, going from 2 to 3 to 5 to 10 users really shouldn't be any issue. Let's see what happens.

Of course to really scale to, say, even a dozen users, we will want to port this project to the cloud. We could very well make that a _PART 2_ of this project's development, after we have confirmed that the application works great for 2-3 people.

Exciting stuff.

---

## Room Naming.

### Monday, August 15th 4:21PM PST

---

I didn't get done today what I set out to get done last time... HOWEVER... I knew that I really wanted to add a room naming feature, and that it would pragmatically only take me a few hours to do, so I went ahead and added that in today.

I also added a **ValidateInput.java** interface with a couple of validation functions that are common to both _LoginPanel_ and _RoomNamePanel_ to reduce code duplication.

**RoomNamePanel.java** is practically done. We have also done some of the book keeping work in connecting the panel with the flow of the rest of the program.

**Next time**, we will flesh out the _JoinRoomWorker_ (...for real this time.) Either that day or the next will likely require some extensive testing to iron out some bugs, as they will undoubtedly be some to take care of.

Until next time.

---

## JRW done.. Moving on to the sending of messages.

### Tuesday, August 16th 3:17PM PST

---

I will admit at this point that I have put off testing my code for a number of days. This may prove to be unwise in the near future. That said, I am working to get my code to the point where I can test the sending and receiving of messages. I suppose the program will mostly be done at that point, if we're being honest with ourselves.

Again, this could prove to be unwise. Perhaps not the most advisable strategy to most developers. That said, I have a pretty solid gameplan in my head of what pieces need to be put in place. The application I am putting together makes perfect sense in my own head. If it didn't, I would surely be spending a heck of a lot more time testing and designing my architecture. Even on that note, during times where I have sensed that the architecture is off, I have self-identified the offness and taken a little time to make some design revisions. I would like to think that I am relatively okay at catching myself quite early with writing nonsensical code before I go too far down the rabbit hole. I wasn't always like this. Perhaps I can attribute this to my own recent development in improved logic and ability to break things down in a procedural manner within the confines of my own head as well as a pencil and paper.

**JoinRoomWorker.java** is done. In fact, that code was relatively easy to complete. That said, I have yet to tie in _SessionCoordinator's_ response code to JRW's requesting. This shouldn't be hard at all, as it will be almost identical to the setup of a room host. I will probably do some refactoring on-the-go as I put that in place to reduce code duplication.

As of now, I have shifted my focus to linking **ChatWindow.java** up to the _ChatUser_ and it's net capabilities to get it talking with other participants in the chat room. This will set me up to have Alice and Bob sending each other messages, which, once we have this completed, the app will be practically 80%-85% complete. We are getting close.

To make the above fall into place, my next step is to flesh out a _UserOutputHandler_ thread worker that handles _ChatWindow_ events (such as a text-based message) by passing them along to the ChatUser's _OutputWorker_. Essentially, we will have an action listener defined in the chat window that wakes up the UOH worker via the wait()/notify() mechanism; this will tell UOH that it has a message to send. UOH fetches the text from the window's text field (it can do this, as I have defined it as a nested class within _ChatWindow_), after which it packages the message into the form of a Chatter message (basically just attaching a timestamp and who is sending it) and inserts said message into the ChatUser's OutputWorker's outgoing message queue, notifying the OW that it has a message to send.

UOH will be able to access OW's message queue by way of a method defined within _ChatUser_, which will be made a field variable inside _ChatWindow_ as a handy reference object. This should actually work quite well.

With this plan in place, I am in the position to bring this code into existence the next time I have time to work on it, which will at the latest be Thursday. I may have time tomorrow, but I also have to meal prep, which is a time consuming process. Who knows, I may still have time to code, all priorities considered (i.e., sleep, exercise, etc).

---

## User IO (Mostly) Taken Care Of

### Wednesday, August 17th 4:10PM PST

---

I say mostly because I have yet to test the code. As previously mentioned, I have been putting off testing for a number of days at this point.

Now that I am at the point where I have completed my previously laid out objective to a satisfactory degree and even patched up code in some other places, I am at the point where I think it would be wise to lean into some test-driven development for a day or two. Surely, I will have a number of bugs to work out, as that is just the nature of how programming works.

The cool thing about test-driven development is that, given the notion that I have an expectation inside my own head of how I expect the application to work in an abstract sense, I can compare that idea in my head to what I observe to be happening, and go from there in applying fixes. I am almost certain that I will be spending all of my programming time tomorrow (and probably some the next day) ironing out bugs. In doing so, I will take care to track notable issues that take a little bit more time to figure out. That said, I am confident in my overall bug-fixing ability, particularly with the way I have laid my code out.

Of course, working through network-related bugs will be a bit more tedious in solving, though I have definitely developed my own method of solving these types of bugs. VS Code itself has proven to be a brilliant editor in this sense, as I am able to a compound launch debug session, which allows me to run multiple programs simultaneously in debug mode, catering to all the various breakpoints littered across the programs I am running. This has proven to be absolutely invaluable in stepping through protocol-centric conversations between entities across a network; with breakpoints set up on either end at all the various potentially problematic points that I wish to investigate at a particular moment, I can simply hit the green arrow and sequentially proceed from breakpoint to breakpoint, bouncing between programs in a seamlessly fluid manner. I absolutely love it.

Until tomorrow! Time for some debugging.... Hehehe

---

## Debugging, Pt 1

### Thursday, August 18th 5:14PM PST

---

Most of today went pretty smoothly on the debugging front, until I hit **RoomNamePanel.java**. Today was actually the first time seeing the panel, and so more than half of my programming time was spent simply tweaking the layout of said panel. It could be better but I've gotten it to a relatively acceptable point where it is now.

The bug/behavior that was the most-time consuming for me to figure out was a rather silly bug, arguably perhaps not even a bug, but a desired behavior nonetheless. Part of the _RoomNamePanel's_ functionality is to display a red line of text underneath the text field whenever the user attempts to supply "bad" input (which in this case is any non-alphanumeric string outside of the range of 2-16 characters long), informing the user that their input requires tweaking.

My desired behavior was to have this red line of text to appear without changing the layout of the other components that were within the panel. After 30 or so minutes of wrestling with the GridBagLayout manager, I found an answer on StackOverflow that suggested changing the text value in the JLabel from the warning text to " ", instead of flipping the visibility between true and false. This did the trick.

Tomorrow, I will move on to verifying the functionality of the buttons on _RoomNamePanel_, after which I will move on to verifying the functionality of _ChatWindow_.

Until next time.

---

## Bugs. A lot of bugs. But we're fixing them.

### Friday, August 19th 4:58PM PST

---

Title says it all. I must have neutralized around 25 bugs in 2 hours, some more significant than others, some tricky, some not. I probably forgot to synchronize on wait() or notify() calls half a dozen times. That is still a habit I need to adopt, apparently. Luckily, that one is an extremely easy bug to identify and fix.

For next time, the last bug I solved today is on line 129 of _initializeUser()_ of **SessionCoordinator.java**; the chat room is loading up. We are getting closer and closer. Just have to keep plugging away.

---

## Progress.

### Saturday, August 20th

---

**What got done:**

-   Fixed the remainder of the outstanding bugs.
-   Chat session title is now correct.
-   Bob can now accurately see Alice's room listing in _RoomSelectPanel_.

Big progress being made in a short amount of dev time today. Only 90 minutes, As I spent the first 2.5 hours here at the coffee shop going into great depth about my future in the coming weeks, months, and years. Sure, I sacrificed some development time to do so, but I would like to think that that kind of stuff is worth being done every once in a while.

Off to dinner with my dad. I plan on doing a bit more work tonight if I am up for it. Then again, I may end up crashing early, as I never really got to catch up on sleep this morning.

**To Do Next**:

-   Get IP address displaying correctly in the room listing information (i.e., change "0.0.0.0/0.0.0.0" to "0.0.0.0")
-   Implement the joining of a room.

Every day, we inch closer and closer to the finish line. Tomorrow will be a huge day.

---

## Frustrating.

### Sunday, August 21st 9:31PM PST

---

Instead of worrying about the minutia that were noted as things to do next from last time, I am going to cut straight into the meat & potatoes of crucial tasks that need to be implemented as soon as possible.

**To do today:**

-   implement the sending a message.
-   add "Join Room" functionality to allow Bob to join Alice's room.
-   depending on how much time we have, test both functionalities.

so sending a message was already mostly implemented, but text doesn't seem to want to appear in the models I have being shown in my _JLists_. Seems to have something to do with needing to update UI properties using a _SwingWorker_. Sigh...........

I guess I know what I'm working on tomorrow.

---

## Progress.

### Monday, August 22nd 4:18PM PST

---

**What got done:**

-   After several days of debugging in frustration, I **finally** figured out how to get _JList_ text to update for _outgoing messages being sent by a user_. (i.e., "\[_timestamp_\] You: Hello!). All the answers I saw on StackOverflow pointed to calling _ensureIndexIsVisible(row)_, but this function call never did anything for me. I did notice, however, that the text would appear after clicking, so I thought to myself, "what if I just make a function call that allocates focus to the JList after a message is sent?", and sure enough, it worked like a charm. Very very relieved. That was one of the tougher bugs I've had to solve so far with this project. When it comes time to getting Bob and Alice talking, we may have to make a similar adjustment in a different function call.
-   A lot of the user joining code is in place. I didn't get to spend that much time on it today, as most of my time was spent in solving the aforementioned bug. That said, the code was already 50-60% done before touching it today; now, it's probably closer to 90-95% done in getting a user successfully joined. _JoinRoomWorker's_ communication with _SessionCoordinator_ is squared away; additionally, we have _Registry's_ user and room participant information being successfully updated by way of protocol messaging via the SC. The **last** bit of code for this part is literally two lines, instantiating the _JoinRoomWorker_ and calling **start()** on the "Join Room" button click within _RoomSelectPanel_. That's it. Of course, we will need to debug. Next to zero chance that the code works right off the bat. That being said, debugging is a part of game, it is nothing new, and I am confident in my ability to solve any issues that come my way next time I get to touch the code, which will likely be tomorrow.

More progress. Inching toward our objective. I think we are on track for finishing a functional version of the application by the end of the month. It should really only take a 3-5 more days, so we should even have a few days in reserve. Let's keep on trucking and get it done.

Out.

---

## Bugs: Getting closer.

### Thursday, August 25th 2:39PM PST

---

I spent 3 hours yesterday working through and debugging network code, to the point where I came to the conclusion that the way I had come to build some things in the way I did, specificalyl regarding _SessionCoordinator_ and _ChatUser_, are a little off. The foundational components for the communication channels are excellent and seem to be quite robust for the most part. The weakness in my current approach, as it stands, seems to be rooted in the interface that I have laid out for messages to be sent and received. It could certainly be formalized to a much greater extent.

This is what we are now going to work on: Formalizing some message types into a serializable format.

As there is a chance that this attempt may flop (though I am about 90% confident in my ability to make it work and to make it work well), I will be starting and checking out a new branch to develop this, "serialized".

### 4:54PM PST

After a solid 2+ hours of development, I am essentially back to where I was yesterday in terms of program output, but with a much more solid plan of what needs to be done.

The _Message_ class has been subclassed by 4 different classes at this point, 2 of which have been tested and both seem to work like a dream (_NewRoom_ and _NewUser_). Now that we have gotten to _JoinRoom_, I am realizing now that I have to seriously re-work how the **SessionCoordinator.java** class is set up.

Given that I am starting to get hungry, now would be a great time to go to the gym, and let my mind do some subconscious processing on how I might best proceed in re-working the _SessionCoordinator_.

At a cursory level, its functionality hasn't changed much; SC starts by first communicating with the host of the room that it has been assigned to; after sending a _WelcomeMessage_ to said room host, it then sits on standby, waiting for either a _JoinRoomMessage_ or a _ExitRoomMessage_ to come along.

I, more or less, know what needs to be done. I just think I need to step away for a bit and come back fresh after some exercise, so I can potentially chip away some more before going to bed tonight. If not, I will certainly be putting in more hours tomorrow. Tonight would be good though.

Out.

---

## Tired, but trucking away.

### Friday, August 26th 5:04PM PST

---

Body is in need of a really good sleep tonight, so that is what I aim to provide it with.

Code is on track to being good and workable, it is simply in a state of experiencing large change that is difficult to measure and observe. I will be in a much better spot to report on the changes tomorrow after some food and rest.

2 more hours logged today.

---

## Early Saturday, Good Progress.

### Saturday, August 27th 7:50AM PST

---

Woke up at 5AM this morning to get to the coffee shop by 6am so I could get into a flow before it got busy.

It seemed to pay off nicely. Closing in on 2 hours of development, and I have completely cleaned up the _SessionCoordinator_, all fitted out with the brand-new messaging system that I came up with over the past few days. The code looks a trillion times cleaner; not only will the code be easier to debug, but the logic of _SessionCoordinator_ has been cleaned up as well.

Previously, I was experiencing hangups at various endpoints of the communication system I had concocted. With the way I have laid things out, I suspect the number of hangups to go down drastically; if and when we get some more, I suspect them to be much, much easier to debug.

Things are looking up early on a Saturday. Next on the agenda is 90 minutes of problem-solving. After that, I will be headed to the gym for a work break, eating some food, then back to this project.

Next to-do is cleaning up the Input and Output handlers, which are those classes responsible for actually processing the messages. I suspect this portion will only take 15-30 minutes to clean up. Then, it will be on to re-formatting the Registry, which could take up to an hour. After that, we will be more or less onto
testing.

### More Big Progress.

Today was pretty productive. I got a couple of multi-hour coding sessions in. We got a ton of stuff done. Tomorrow, I will need to work just as hard and consistent, perhaps even more so.

The new messaging system is almost completely worked in; Registry and SessionCoordinator have both been completely re-worked for all the different message types. Room listing fetches and refreshes have been re-worked, as well as user creation, room creation, and joining rooms server-side.

**Tomorrow**, our attention first-thing will be directed toward re-working our last big net-centric entity, the _ChatUser_. I have already made some notes brainstorming as to how I will do it. In fact, the way I have abstracted all the text content away into the _Message_ class should make things much, much simpler.

After re-working _ChatUser_, one of the last things we will need to do (before handling room exit events) will be re-working the Input and Output Handlers for _ChatUser_. It really shouldn't be too hard with the way I have laid things out for myself.

I am looking forward to making a bunch more progress tomorrow.

2 hours logged tonight, 2 hours logged this AM, and another hour logged in the early afternoon. Looking to extend that early afternoon session a bit more. It's difficult to find areas that are both comfortable and quiet; I may work out on the deck for a bit tomorrow, as my room is typically quite stuffy and gross.

Out.

---

## Pleasantly Surprised

### Sunday, August 28th 8:22AM PST

---

After tweaking the _ChatUser_ state machine, I realized that the I/O handlers and workers for _ChatUser_ had already previously been adopted completely to take on the more recent messaging system that I proposed a few days ago.

That being said, it appears we are pretty much at the point where we can start testing our code, and seeing if we can get the basic functionality fulfilled. I suspect that, if we are to come across any errors, that they will be much easier to track down this time around with my newly introduced approach to sending and receiving application-wide messages.

### 10:22AM PST

I have made a lot of progress this AM, though it is time to go to church. Currently, the _UserInputWorker_ for Bob (i.e., the Joining User) is blocking at line 42, where _readObject()_ is called. When we come back later today, we will look at **why** this line in particular is blocking.

---

## Major Achievement (with kinks)

### Tuesday, August 30th 7:51AM PST

---

So after rougly 2-3 days of throwing myself at the previously mentioned issue of bob's _UserInputWorker.readObject()_ incessantly blocking when the _OutputWorker_ at the other end had definitely written and flushed the welcome message at the other end, combing through the StackOverflow archives for people with similar issues, I came to the conclusion that I had **overengineered my solution**. In other words, I had made things more complicated then they needed to be.

Essentially, this was my problem-solving approach: I knew that the first Socket connection between _SessionCoordinator_ and the room host (i.e., Alice) was clean & mean. No issues whatsoever there. This incessant read-blocking issue has _strictly_ been a problem with **joining a pre-existing chat room**; joining a room is handled by _JoinRoomWorker_, whereas setting up a room as the host is handled by _RoomSetupWorker_.

The difference? RSW passes connection info along to _ChatUser_ (i.e., IP and port number), giving CU the responsibility of making that initial connection themselves with the SC, **whereas** JRW was actually **first** making the connection with SC, doing some work, _and then_ passing connection info along to the _ChatUser_ that was joining (i.e., Bob). I tried several variations of the connection information pass-off to Bob:

-   passing the already-connected Socket to Bob, using _initSessionSocket(Socket)_ to do so, then getting Bob to try and use said Socket.
-   getting JRW to close the Socket, pass the session connection information off to Bob using _initSessionInformation(sessionIP,sessionPort)_, and getting Bob to create their own Socket using said information.
-   repeating the previous step, but omitting to close the Socket that JRW used.
-   ... and probably a few other variations that I have slipped my mind.

_None of these worked!_

After going home frustrated a few nights in a row with little to no progress, I realized walking from my car after having parked... _If a system isn't working, try simplifying the system_. I realized that I had likely overengineered my proposed solution, and that I was introducing considerably more complexity into the app than what was truly necessary.

So what did I need to simplify? Well, room creation + the host joining their own room seemed to be working perfectly fine. It was non-hosts joining pre-existing rooms that needed to be simplified.

So, what did I do? Well, I moved essentially 98% JRW's workload into _ChatUser_, giving CU the autonomy of setting up the initial connection with SessionCoordinator on their own. This, of course, required some modification to the overall state machine within _ChatterApp.main()_; I created a new state, "JOINING*ROOM", where the ChatUser thread can be started, then made sure that the \_chatUser.start()* in the "CHATTING" code chunk would only fire for room hosts.

This introduced one other slight problem into the mixture; _ChatWindow_ was being initialized inside the "CHATTING" code block, _before_ ChatUser's thread was being started. Now that I had introduced the "JOINING_ROOM" state for non-hosts where the ChatUser thread can be started from as well, I had to introduce a mechanism to keep non-hosts from messing with the ChatWindow **before** initialization.

I could have just copied the window initialization code and had it in two places, but seeing as how that would violate DRY, I decided to try some concurrency "magic" with wait()/notify().

I introduced a new field variable in _ChatterApp.java_ called _mainAppNotifier_; this notifier would act as a two-way communication mechanism for ChatUsers and their respective ChatterApps. In other words, _ChatUser_ would **wait** before opening up any of its communication pathways with the SessionCoordinator, _until_ it had been notified from main(); I strategically placed this notify() so that it occurs both a) after ChatWindow initialization, and b) after both _ChatUser.start()_ calls (inside "CHATTING" for hosts and "JOINING_ROOM" for non-hosts).

Instead of dealing with more complications of context switching, I decided to eliminate the "JOINING_ROOM" state, putting hosts and non-hosts through the same line of processing, further simplifying my program. Sure, maybe this less closely approximates what is happening inside the application, but I would argue that program functionality far outweighs the desire to mirror the application's code with what is "happening" in an abstract sense.

Sure, maybe a non-host ChatUser that enters the "CHATTING" state code block isn't technically chatting right away, but then again, neither is the host! Both user types must perform their own forms of initial setup to prepare themselves for the process of sending and receiving messages.

As it stands, not only have I been able to get Bob successfully joining Alice's room (which is confirmed by Bob receiving a proper WelcomeMessage from the SessionCoordinator), but **Bob and Alice are successfully sending and receiving messages to and from one another!!!** This is a MASSIVE win. While it took me a few days longer than I would have liked, I am ecstatic that I finally got things working.

Now... We have a few kinks and bugs to iron out. Most of them should be relatively easy, with one or two that may prove to be a tad more difficult:

-   Welcome and JoinNotify messages should add participant names to the participant list (EASY)

_This one will literally just involve strategically adding a few method calls in the UserInputHandler method where incoming messages are handled. Pretty straightforward._

-   Avoid sending messages to unintended receivers (i.e., the sender of a message should not receive their own message) (EASY/MODERATE)

_This one will be a tad more difficult, particularly in the sense that there is an easy way to solve it (send duplicate messages and, at the UserInputHandler, discard all messages whose sender matches the associated user alias of that ChatterApp process), or a more moderately difficult way of solving it (modify SessionCoordinator's message routing protocol to be slightly more sophisticated). In the end, going with the moderate approach will be far more scalable as we will avoid using up CPU time on sending unwanted messages. It may take a tiny bit more time to figure out, as I will need to sketch some thoughts down on paper, but I believe it will be worth the extra bit of time in the end._

-   Get text updating in the Chat Feed without needing to click on the window (HARD)

_I spent several days previously trying to figure this one out, and had even thought I had come up with a solution, albeit a bit of a hacky one. Now, I'm realizing that it doesn't seem to work in all cases. Depending on how much this trouble this one gives me after I've tackled the other ones, I may even decide to leave it until after I have implemented the rest of the application functionality; namely room exits, host changes, and room re-joining._

**After this**

-   Room exits
-   Host changes (triggered by host exits)
-   Joining a room after having previously exited another

Once these bullets are tackled, our app will more or less be finished.

### 2:03PM PST

This has been one of our most productive days so far in the entire two months of development.

Not only did I address a major bug that had been holding me back for 2-3 days, but I've also managed to tackle 2-3 more issues:

-   Welcome & JoinNotify messages now both correctly add the appropriate names to the list of participants in the chat. This was accomplished by not only adding calls _addParticipantName(String)_ in _UserInputHandler_, but also adding an ArrayList<String> field to WelcomeMessages detailing the current list of participants in the chat, which seems to be crucial thing to communicate to newly joined users of a chat.
-   A more sophisticated message-routing protocol was developed based on _isSingleShot()_ (reference **Message.java** for more details).

That's it for now. Getting to the last few things on the list here.

Out.

---

## Slow & Steady.

### Thursday, September 1st 4:49PM PST 2022

---

I was extremely tired today and didn't feel like coding at all. Even yesterday, I was pretty tired. That's what happens when you wake up between 3-4am to get to work early so you can get off early to code while it's still light out. Such is life.

Despite not really being into it mentally, I still made some progress. The code output has been slow, as the actual code has been more touch-and-go.

At this point, 98% of the application is built; I simply need to add a few methods here and there to put the finishing touches in place.

For instance, *ExitRoomMessage* and *ExitRoomWorker* were both completed today. Super small classes. The "Exit" button in *ChatWindow* spawns and starts an ERW, which takes in a *ChatUser* reference to initialize itself. In its start() method, all it does is create an ERM and push said message into *ChatUser's* outgoing message queue. The reasoning behind creating a thread worker to carry out 4-5 lines of code is to eliminate any potential hangup that could occur as a result of event listening, which would be more likely to occur if the task to be carried out by the thread was a more time-intensive procedure. ERW also calls *markChatRoomLeft()*, which simply flips *ChatUser's* **isChatting** flag to false. This is important as, when ChatUser is notified out of it's wait() call, we want to make sure it doesn't try to initialize a chat again unless it is truly time to do so.

As one would expect, *UserInputHandler* takes care of message reception, and so an extra conditional block to take care of the ERM response was also written today. In here, all we do is change the application state to point at ChoicePanel, followed by a notify() call to main().

In *ChatterApp.main()*, following the wait that we are notifying out of, we flip ChatWindow's visibility to false, after which we MainWindow's visibility back to **true** and call **dispose()** on ChatWindow to be sure we release any resources that were being used.

**For Next Time:**

- Implement SessionCoordinator's response to ChatUser's exit request, which will lead us into implementing Registry's response to the exit request as well (i.e., book keeping)
- Add in some thread cleanup on the side of *ChatUser*.


---

## Good Day of Development

### Saturday, September 3rd 12:55PM PST 2022

---

Today has been a "so far, so good" kind of vibe. In the 2.5 hours that I spent programming this morning, 90 of those minutes having been spent working on this project (as I spent 60 minutes doing some problem-solving, Leetcode style), the output has been relatively fruitful.

Last time, my efforts were directed mostly at taking care of the *ChatUser* side of things with respect to getting a user out of the chat room. Today, the focus was purely on *SessionCoordinator's* role in getting said user out of the chat room.

Without having tested any of the code, I'd say I'm about 90-95% done SC's side of things with respect to getting a user out of the chat room. The last thing I need to add, as far as I can tell, is addition of another Message implementation, *HostChangeMessage*, strictly in cases where the person leaving the room is the host. In cases such as these, we want to make a host change; essentially we will assign this role to the next user that had joined right after the host, which will be easy to tell, as users are added to SC's user list in the order in which they joined.

After adding in this "host change" code, SC's side of things for handling room exits will be practically done.

From there, the last piece will be *Registry's* side of handling room exits, which will be comparably light (SC's load is the heaviest and most involved out of the 3 entities considered in this ordeal). For the Registry regarding room exits, we are really just concerned with book keeping. The goal on this end is to have participant counts and room existence up-to-date, so anyone looking at rooms to join is able to see the most up-to-date version.

That considered, we will want to add some validation code somewhere that ensure that the room a user attempts to join is actually a valid room, and not just a ghost room the user is seeing because they're looking at an invalid rooms list (which can happen if the user doesn't refresh the list for a while). We will get to this after the Registry and SC sides of things are taken care of.

After having a coffee chat with a friend and a gym break, we will be back to the grind for another 2-3 hours of coding later in the afternoon/evening. Until then!

Yeti, out.

### 5:40PM PST

Much more tired than I would have bargained for on a Saturday evening. I also have to pick up my mother from the airport, still have to go to the gym, and would like to pick up a baked chicken from Superstore after the gym; the past two weeks, they have been out of baked chickens by the time I get there.. **Sigh**. I really only got another hour of dev time in this evening,
and a decent chunk of it was spent with my having my eyes closed. But it wasn't all wasted time.

I realized that a big part of the way I had laid out my *SessionCoordinator* logic for adding and, particularly, removing users, based on *participantCount*, really opened me up for unexpected and undesirable behavior in the future.

Worker ID numbers are a crucial component of the communication pipeline established by the Coordinator. Essentially, we use a single integer value to map a user within a given chat room to every other communication entity within that chat room. And thus, these worker ID numbers **must** be unique.

With each new user that I had joining a *SessionCoordinator's* chat room, I was assigning workerID numbers based on participant count. This operates on the assumption that no one has left the room. Upon further examination, this has proven to be a poor assumption to operate on. Given that I have yet to test and operate my code having users join and leave rooms to enter other rooms, the poor state of this assumption and the unpredictable behavior that would result had yet to be experienced. Essentially, by being introspective and reflective on the nature of my code, I have effectively nipped a major system flaw in the bud before it even came to fruition to rear its ugly head.

Let's go over a quick example to illustrate the flaw in my previous line of thinking. Say Alice, Bob and Charlie are all in a room chatting. Bob leaves. Participant count goes from 3 to 2. After Bob leaves, Dennis joins. The system assigns a workerID number to Dennis (and his associated workers) based on participant count, which means his workerID number would be 2. But Charlie's worker ID number is 2. These numbers **have** to be unique, as task numbers are received in the system as to indicate which incoming message queues have messages to be forwarded by the MessageRouter. 

**And so**, The *SessionInputWorker* for Dennis would put up a task according to the worker ID number that had been assigned to it (i.e., 2), and the Router that picks up this task would be wildly confused when it went to Charlie's incoming message queue and found nothing (or maybe it did find something to forward). **Either way**, messages sent by Dennis would go completely missed by the system.

**Now**, to avoid these mishaps, we keep an additional variable in *SessionCoordinator* called *nextWorkerID*. With each new user that joins the chat room, this number first assigned to the user that joins, and is then incremented. It does not get decremented when users leave, as is the case with *participantCount*. With this in mind, the host worker ID won't always be 0 either.

Next time, we will have to spend another hour or two refining the *SessionCoordinator* code to accomodate the changes noted above to have the program operate as expected with users coming and going.

---

## Adversity.

### Tuesday, September 6th 3:43PM PST 2022

---

This past weekend, I had high expectations for myself. I went in with the intention of finishing the project entirely. However, one thing led to another, and it didn't get done. In fact, my productivity flopped quite hard.

I spent a good deal of time today reflecting and journalling about why I thought this to be the case. I came to what I think to be a pretty sound conclusion. Essentially, by proposing grand expectations for myself without actually putting a pragmatic plan in place as to how I would be spending my time for the weekend, particularly with respect to other responsibilities, I essentially set myself up for failure, as I left the certainty of my completing my objectives completely in the void.

Grand expectations are totally fine. However, if big goals are left just as they are without adequate time spent planning how those goals are going to be achieved, then uncertainty and anxiety go through the roof, the mind is left to wander and conjure up all sorts of reasons and irrational lines of thought as to why something can or can't be done, and a person is left to the whims of their own mind's devices.

It is my belief that humans are naturally anxious and paranoid beings. If a person is not anxious or paranoid, it is likely because that person has either systematized a way of keeping their anxieties at bay (i.e., exercising regularly, meditating, journaling), or they have gotten adequately good at temporarily fooling themselves into being disillusioned that they aren't, in fact, feeling any kind of anxiety whatsoever. Experiencing anxiety and failure are a part of the human experience; it's how we deal with these things that can make or break our success in the journey of life.

### Code...

Turns out brushing up the *SessionCoordinator* class didn't take nearly as long as I thought it would. Looking back, I definitely gave myself much more time than I thought I would need; I try to do this as much as I can, as I have gotten so used to doing the opposite (i.e., giving myself less time for a task than is pragmatically required).

Picking up from the bullets we left on Thursday, or next task at hand is programming the Registry response to the SessionCoordinator notifying them that a ChatUser is leaving a particular room. After that, we are to proceed in circling back to *ChatUser* so we can articulate some thread clean up.

### 4:47PM PST

Fortunately, the last bits of code that I have had to put together have been pretty short bits of code and have been pretty straightforward to articulate.

I probably mentioned this earlier, but in reflecting on the behavior of my program, there is a room for unexpected behavior and/or crashes regarding the RoomSelectionPanel. Essentially,  between the time that the user presses "Refresh" and the point in time where they select a room and click "Join", the room they selected could have been closed. This can lead to ArrayIndexOutOfBoundsExceptions, as well as other undesirable behavior, such as joining a room that wasn't even selected in the first place.

This fix might take a tiny bit of time to put together; however, it is more than possible to reason together. The first thing we have to do is, upon observing a user select a room, we save the name of the room that they have selected. Then, for every time the "Join" button is pressed, we save the index of the current selection, then we force a Refresh request and do a few checks. First, is the selected index still valid? If it is, does the room at the corresponding index in the recently fetched list match up with the room name we saved earlier during selection? If both of these come out to be true, then we can proceed with the room join. 

If either are false, we have to find another way of seeking out the room that the user wanted to join in the first place. Linear search is an obvious first thing to do; for the sake of correctness, we will probably just do a linear search for now. However, for dealing with a larger number of rooms, we could easily add another Message type, such as RoomQueryMessage, where we query the Registry about the existence of a particular room. Registry has HashMaps keyed on room names, thus the lookup on that end is O(1); we would just have to deal with the extra network I/O time of sending and receiving, which all in all could be worse.

Things to think about and implement for next time.

---

## Tons of Progress Being Made. So, so close.

### Wednesday, September 7th 4:50PM PST 2022

---

We are getting so, so close to the end of the timeline here.

I've implemented pretty much the last of the features. All the coding at this point is mostly touch-ups.

Currently, I am trucking through adding a built-in mechanism to the room joining process that will stop a user from trying to join a room that doesn't exist. This could happen if, between the time a user selects a room from the room list and hits "Join", all users in said room take their leave. This is an edge case behavior that I would likely have a hard time reproducing in a test environment and would really only come up in live use case situations, and if it did, the app would either crash or produce unpredictable behavior. This fix will be one of the last few patches we add on to Chatter, and we are just about complete with the code for it.

Once this is done (which it will most certainly by the time my next session is over), I will spend an hour or so testing the app, maybe even with 3-4 users. I probably won't test it much beyond this (at least for now). After this, my **last** patch will be to add in some thread pools for *MessageRouter* and *RequestHandler*. This, again, will be a move to make the app more scalable for a larger user base. Realistically, we don't need a *MessageRouter* for every new user. By working with a pool of MessageRouters, we can avoid a large number of startup and shutdown times for users joining and leaving roomms. We can achieve a similar optimization with the *Registry* by working with a pool of *RequestHandlers* instead of spinning up a thread for every new request; instead, we could come up with a relatively rudimentary thread pool protocol that shrinks and expands based on simple flow parameters. This will be our last programmming task.

All done for now. Time for the gym.

---

## Features are practically done.

### Thursday, September 8th 4:40PM PST 2022

---

I finished building the room pre-join validation mechanism today. It wasn't quite as trivial as I initially thought it would be. The approach I had spent 1-2 hours brainstorming and building yesterday turned out to have introduced unnecessary complexity (3 entities and 3 objects being waited/notified on), which as it turned out, really left the execution of my code up to runtime optimization. In other words, I was hitting deadlocks.

What did I do? Well, drawing on my previous learning experience from earlier on in the project, I had two options: further complicate the abstraction to make it work, or simplify and change the approach completely. I chose the latter.

Instead of going with three entities (*RoomSelectPanel*, *RoomsListFetcher*, and *PreJoinWorker*), I opted to only use the first two in tandem with a shared validation object consisting of a few flags. Essentially, before waking up the RLF for a refresh, the RSP thread would toggle a "room validation request" on this shared object. Post-refresh, RLF essentially checks this object every time, and if it comes up true, it performs a few extra steps. That shared object is also how the result of the validation is communicated. Additionally, waiting and notifying between just the two entities instead of three proves to be a heck of a lot more predictable, so far at least.

Now, the validation mechanism seems to work. And by that, I mean I can get Bob into Alice's room with the validation mechanism in place and nothing breaks (lol). The next step tomorrow in testing will be to see if the validation mechanism actually works to detect a room that no longer exists and properly notifies the user joining.We also have another bug that seems to occur inconsistently where a user's name (sometimes) will be duplicated in its own participants list upon joining a chat. To address this, we will see if we can opt to use some kind of ordered HashSet for the participant list model instead of an ArrayList. Whether or not this will be possible is completely unknown; we will find out tomorrow.

Good amount of progress for the day. Excellent level of focus and output. Feeling good.

---

## Massive Breakthrough

### Monday, September 12th 3:46PM PST

---

I am beginning to cultivate considerable respect for the practice of multithreaded programming. Here, I spent all this time thinking I was all fine and dandy, seeing as how I was able to get a multithreaded networked system up and running with a handful of different entities. 

Well, I felt this way until I got to the thread cleanup phase. I spent a week literally just figuring out how to shut down my threads and dispose of their resources properly.

The method calls are one thing. They're actually very simple and straightforward. One of the non-trivial aspects to thread shut down and clean up, however, it making sure it doesn't happen too early. 

I spent I don't even know how many days trying to figure out why the *ExitRoomMessage* sent by ChatUser wasn't being returned. I mean, it was working just fine *before I added any of the shutdown code*.

I had gotten this righteous idea stuck in my head that **because** SessionCoordinator was the technical *owner*, if you will, of the threaded workers employed for it that it should also be responsible for shutting them all down. 

This is a nice idea in theory, but in practice, it introduces race conditions.

More precisely, the *MessageRouter* is in a race to get its last task done to forward any remaining messages before it is shut down by *SessionCoordinator*, including the *ExitRoomMessage* that the ChatUser **must** receive before it signals its own shut down.

Similarly, the SC's *OutputWorker* is in a race to write and flush the ERM passed along by *MessageRouter* (assuming it was successful in passing it along) before it is shut down by *SessionCoordinator*.

I was stuck on this for a number of days. Many of these days were spent simply trying to pinpoint the exact location in the socket communication pipeline where things were breaking down.

When I put a breakpoint on the function call *shutDownWorkers()* (a pretty self-explanatory function), the ERM to go out was still sitting in the incoming message queue where it had been placed by *SessionCoordinator*. In other words, *MessageRouter* and SC's *OutputWorker* seemed to getting shut down **long** before they had any chance of sending out the last set of messages. Bingo!

So the problem had been located... So now what? Well, a clear issue had been identified with my shut down procedure. That said, I had to change the way in which I was shutting down my *SessionCoordinator* workers (I haven't even gotten to the *ChatUser* workers yet. Bear in mind, however, that these workers should be 1000x easier to gracefully shut down, based on the notion that all of ChatUser's workers can be signalled for shut down once the *UserInputHandler* receives the response ERM with no glaring issues, at least not yet).

So what did I do? Well I thought, if *ExitRoomMessages* are so important, why not just add an if-statement that checks for them within *MessageRouter* and *OutputWorker* classes, such that either class signals itself for a shut down upon observing an ERM? This way, I can call *join()* on both thread workers from *SessionCoordinator* to ensure I'm not removing or closing anything I shouldn't be before those threads are officially dead. Turns out this works great. Sure, maybe it's not the cleanest thing in the world, but as far as I can tell, it's functional, and functional is more important than pretty. Functionally pretty would be ideal, but we don't live in a utopia. We do the best we can with the time we have. Any other way leads to frustrated perfectionism, at least that's what I've found in my discoveries in the context of solo projects on limited timelines.

So that's it for today. Very excited to have made this break through. Of course, I will still have to test whether the room closure is detected by Bob's client when he tries to join Alice's room after she has left. We will have to see how that plays out tomorrow. Either way, this was a **major** breakthrough.

Yeti out.

---

## Trucking along...

### Tuesday, September 13th 3:06PM PST 2022

---

Today, we worked on graceful shutdown of ChatUser worker threads. The code isn't perfect, but it's functional. The code operates on the assumption that the only instance in which we will ever have an IOException is when we have a controlled shutdown. In the current state of the code base, this is perfectly fine. If we were to extend the application to support more users with higher network variability, we would likely want to have a little more infrastructure in place to get a pulse on when connections are shut down and why. But that's not really a concern of mine, at least in terms of code production in the "now". A good thought exercise nonetheless.

Tomorrow, we will come in with the aim of getting back to working on the non-existent room detection and correction, so as to stop a user from joining a room that doesn't exist.

Once this mechanism is in place, we will call the project "Done for Now", prompting a finish-up on final documentation, reflections and write-ups. We may add cloud support 6ish weeks down the line. I think that would be cool. Before we get to that, however, I would like to work on some web development for a bit in updating my website.

Yeti out.

---

## Zero Progress, Ended with an Epiphany

### Wednesday, September 14th 8:48PM PST

---

Today during my 2.5 hour coding session after my day job, I got next to nothing done. Literally no progress. Well, no output at least.

The code I was working involves updating a JTable *DefaultTableModel*, essentially performing a refresh by comparing the latest set of data sent over the wire from *Registry* with the current data. With the current test case, I am simply looking to have my program detect that a row is missing, deleting said row from the model and alerting the user that the room they are trying to join is no longer in existence.

When I tried to simply remove the row, I encountered an unnaturally illogical *ArrayIndexOutOfBoundsException* that didn't even really make sense, as I was using an index of 0 on an model with 1 row. 

Upon digging further, I discovered that the nature of what I was trying to accomplish demanded that I interact with Java's **Event Dispatch Thread**, essentially queuing up a GUI-related task asynchronously so as to minimize strange bugs like the one I encountered while maximizing thread safety. I learned about the *SwingWorker* and how it can be used for longer tasks, as well as *invokeLater(Runnable)* for shorter ones.

In my case, literally all I'm looking to do is remove some rows from a table model, which I don't quite think merits involving a *SwingWorker*. 

So I enclosed the row removal code into an *invokeLater* call, and sure enough, **the model never updates**! I even tried adding a 750ms sleep call directly after, in the odd case that the EDT was somehow backed up and needed some time to process events (which seemed extremely unlikely anyway but didn't hurt to try). Nope, nothing.

Another post I found online suggested that if I call *invokeLater* from a thread that happens to be an *EventDispatchThread* itself, **this** can cause undesirable behavior as well. So I checked this using *SwingUtilities.isEventDispatchThread()*, to no avail.

I really really wanted to figure this thing out tonight. A big part of me wanted to play hero and stay up all night trying to fix it. But at the same time, the pragmatic side of me knows that this would probably do more harm than good in the long run seeing how I have to be up in under 7 hours and I still have my nightly reading to do.

So, that was that. In a feat of desperation, I began the mental process of accepting my defeat for the night. A bitter pill to swallow.

Just as I was about to close my laptop, I figured it would probably do me some good to post a question on reddit.com/r/learnjava or something like that. I thought about *StackOverflow*, but questions need to be phrased *so* carefully on there to keep angry cheeto-dusted firestorms from shooting off out of nowhere. Given my limited time and patience, I settled on Reddit.

As I was coming up with a title for my post, I thought about what code I wanted to copy and paste to articulate my issue. The issue was really completely centered around this method named *serviceRefreshRequest()* in *RoomSelectPanel*. Then through some unconsciously blessed string of thoughts, the likely source of my woes came to me...

The method *serviceRefreshRequest()* was being called from a persistent private static worker thread named *RoomsListFetcher* defined within *RoomSelectPanel*. The design of this fetcher is centered upon wait()/notify(); it waits around until it is notified by some other entity within *RoomSelectPanel* desiring a refresh, upon which it wakes up and performs the refresh.

In the case of performing a refresh directly before a room join **for the purpose of** ensuring that the room to-be-joined is **actually still there**, a function called *attemptRoomJoin(String)* is called from the "Join" button action listener, where the String passed in is the room name to be joined. This function is called **directly** from the action listener. No threading there.

In *attemptRoomJoin(String)*, the way I designed the code is to essentially wait to receive a notify back from *RoomsListFetcher* that the refresh work has been done, and that the decision can be made to either join the existing room or trigger a popup notifying the user that it no longer exists.

In examining this method, I thought to myself... *What if THIS method was being called by the EDT?* Well if that were the case, assuming that I had only Event Dispatch Thread, the EDT would be blocked waiting for a *notify()* from *RoomsListFetcher*, which had made an *invokeLater()* call expecting it to be done within a respectable timespan, so that 20 or 30 lines of code later, it would be able to successfully verify whether or not the room in question actually existed or not.

Turns out, *attemptRoomJoin(String)* WAS an Event Dispatch Thread, which really made me laugh in a relieving way; while I hadn't made any code progress today, I had found the very likely cause to my bug that I had been stuck on all of today, and had been previously putting off as I hadn't had the slightest clue how to deal with it previously, but I'm now at the point where I can no longer put it off.

Do you see the problem here?? I've caught myself in a bit of a partial deadlock; not complete, in the sense that the program cannot progress any longer past a certain point, but partial, in the sense of the following:

Thread B asynchronously passes off an GUI-related invocation to Thread A's task queue, **while** Thread A is stuck waiting for Thread B to be done with its processing, **BUT** Thread B isn't able to actually do said processing correctly until the previously passed off invocation is handled, which can't be handled until Thread A wakes up, which isn't done until after Thread B finishes its unfortunately unsuccessful processing.

So the program never actually deadlocks in the traditional sense, but there is a kind of temporary deadlock occurring that accidentally stops the mysterious Event Dispatch Thread from doing it's job for the worker that so desperately needs it to do its job.

This was destroying me. I'm glad I have gotten to the bottom of *something*.

Unfortunately, none of my GUI-related code is technically Swing safe, which is a terrible thing to think about. A big part of me wants to just fix this bug and mail in the code, but I don't think my conscience will let me do that.

Until tomorrow. Let's get some sleep...

