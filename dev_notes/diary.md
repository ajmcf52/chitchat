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
