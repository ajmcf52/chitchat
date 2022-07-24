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

---
