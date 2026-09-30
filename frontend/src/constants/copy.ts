/**
 * Every user-facing string that isn't a backend message (those are in
 * messages.ts). Components import from here; no sentence is ever typed at a
 * call site, so wording changes in one place.
 */
export const Copy = {
  // The logo is the word plus a yellow full stop - rendered by atoms/Logo.
  brand: {
    name: "seatly",
    mark: ".",
    homeLabel: "Seatly home",
  },

  notFound: {
    title: "This page doesn't exist",
    body: "The link may be broken, or the page may have moved.",
    backHome: "Browse events",
  },

  common: {
    loading: "Loading…",
    retry: "Try again",
    close: "Close",
    cancel: "Cancel",
    back: "Back",
    loadMore: "Load more",
    hostedBy: (name: string) => `Hosted by ${name}`,
  },

  nav: {
    browse: "Browse",
    myRsvps: "My RSVPs",
    dashboard: "Dashboard",
    // One pair of words everywhere - URLs, buttons, links: sign in / sign up / sign out.
    signIn: "Sign in",
    signUp: "Sign up",
    signOut: "Sign out",
    mainNavigation: "Main navigation",
  },

  badge: {
    UPCOMING: "Upcoming",
    CANCELLED: "Cancelled",
    COMPLETED: "Completed",
    CONFIRMED: "Confirmed",
    WAITLISTED: "Waitlisted",
    full: "Full",
    going: "Going",
    ONLINE: "Online",
    PHYSICAL: "In-person",
  },

  tag: (name: string) => `#${name}`,

  live: {
    connecting: "Connecting…",
    live: "Live",
    reconnecting: "Reconnecting — seats may be a moment behind",
    unavailable: "Live updates unavailable",
  },

  seats: {
    left: (available: number, limit: number) =>
      `${available} of ${limit} seats left`,
    onlyLeft: (available: number) =>
      `Only ${available} ${available === 1 ? "seat" : "seats"} left`,
    full: "Full - join the waitlist",
    taken: (taken: number, limit: number) => `${taken}/${limit}`,
    progressLabel: (available: number, limit: number) =>
      `${available} of ${limit} seats available`,
  },

  auth: {
    signInTitle: "Welcome back",
    signInSubtitle: "Sign in to RSVP and manage your events.",
    signUpTitle: "Create your account",
    signUpSubtitle: "RSVP to events and host your own.",
    name: "Name",
    namePlaceholder: "Dulsara Manakal",
    email: "Email",
    emailPlaceholder: "you@example.com",
    password: "Password",
    passwordHint: "At least 8 characters.",
    bio: "Bio (optional)",
    bioPlaceholder: "A line about you, shown to organisers.",
    signInAction: "Sign in",
    signUpAction: "Create account",
    noAccount: "New to Seatly?",
    haveAccount: "Already have an account?",
  },

  browse: {
    overline: "Your calendar",
    title: "Find your next event",
    subtitle:
      "Reserve a seat in a tap. If it's full, you'll hold your place in line.",
    searchPlaceholder: "Search events, venues, descriptions…",
    searchLabel: "Search events",
    modeGroup: "Mode",
    tagsGroup: "Tags",
    clearAll: "Clear all",
    resultCount: (count: number) =>
      `${count} ${count === 1 ? "event" : "events"}`,
    gridTitle: "Upcoming events",
    emptyTitle: "No events match",
    emptyBody: "Try a different search, or clear the filters.",
    noEventsTitle: "No upcoming events yet",
    noEventsBody: "Check back soon - or host the first one.",
    errorTitle: "Couldn't load events",
  },

  detail: {
    backToEvents: "All events",
    about: "About this event",
    tags: "Tags",
    host: "Your host",
    whenLabel: "When",
    whereLabel: "Where",
    online: "Online",
    rsvp: "RSVP - Reserve my seat",
    joinWaitlist: "Join waitlist",
    signInToRsvp: "Sign in to RSVP",
    going: "You're going!",
    cancelRsvp: "Cancel RSVP",
    waitlisted: (position: number) => `You're #${position} on the waitlist`,
    ahead: (ahead: number) =>
      ahead === 0
        ? "You're next in line."
        : `${ahead} ${ahead === 1 ? "person" : "people"} ahead of you.`,
    leaveWaitlist: "Leave waitlist",
    linkLocked: "Link revealed once your spot is confirmed",
    joinMeeting: "Join the meeting",
    yourEvent: "This is your event",
    manageEvent: "Manage event",
    closed: "RSVPs are closed for this event.",
    notFoundTitle: "Event not found",
    errorTitle: "Couldn't load this event",
  },

  toast: {
    confirmed: "You're confirmed! See you there.",
    waitlisted: (position: number) =>
      `Moved to waitlist - position ${position}`,
    rsvpCancelled: "Your RSVP is cancelled.",
    leftWaitlist: "You've left the waitlist.",
    eventCreated: "Event published.",
    seatLimitUpdated: (limit: number) => `Seat limit set to ${limit}.`,
    eventCancelled: "Event cancelled. Attendees will be emailed.",
  },

  myRsvps: {
    title: "My RSVPs",
    subtitle: "Events you're going to, and where you stand in line.",
    confirmedTab: "Confirmed",
    waitlistedTab: "Waitlisted",
    tabsLabel: "RSVP status",
    ahead: (position: number) => `You're #${position}`,
    emptyConfirmedTitle: "No confirmed seats yet",
    emptyConfirmedBody: "Browse events and reserve a seat.",
    emptyWaitlistedTitle: "You're not on any waitlists",
    emptyWaitlistedBody:
      "When an event is full, joining its waitlist holds your place.",
    browseEvents: "Browse events",
    errorTitle: "Couldn't load your RSVPs",
  },

  dashboard: {
    title: "Dashboard",
    subtitle: "Your events at a glance.",
    createEvent: "Create event",
    totalEvents: "Total events",
    confirmedAttendees: "Confirmed attendees",
    openSeats: "Open seats",
    thisWeek: "Upcoming this week",
    myEvents: "My events",
    columnEvent: "Event",
    columnStatus: "Status",
    columnSeats: "Seats",
    manage: "Manage",
    emptyTitle: "You haven't created an event yet",
    emptyBody: "Publish one and people can start reserving seats.",
    errorTitle: "Couldn't load your events",
  },

  eventForm: {
    title: "Create event",
    subtitle: "Attendees see exactly what the preview shows.",
    name: "Event name",
    namePlaceholder: "e.g. Colombo Product People",
    description: "Description",
    descriptionPlaceholder: "What should attendees expect?",
    date: "Date and time",
    seatLimit: "Seat limit",
    mode: "Mode",
    online: "Online",
    inPerson: "In-person",
    location: "Venue address",
    locationPlaceholder: "e.g. Trace Expert City, Colombo",
    meetingLink: "Meeting link",
    meetingLinkPlaceholder: "https://meet.google.com/…",
    meetingLinkHint: "Only confirmed attendees will see this.",
    tags: "Tags",
    tagsHint: "Pick from tags already in use.",
    noTags: "No tags available yet.",
    preview: "Live preview",
    previewName: "Your event name",
    previewHost: "You",
    publish: "Publish event",
    decrease: "Decrease",
    increase: "Increase",
  },

  manage: {
    backToDashboard: "Dashboard",
    seatLimit: "Seat limit",
    apply: "Apply",
    promoteBanner:
      "Increase your seat limit to automatically promote people from the waitlist.",
    // No count until it's known - "(0)" while loading would be a wrong number, not an unknown one.
    confirmedTab: (count: number | undefined) =>
      count === undefined ? "Confirmed" : `Confirmed (${count})`,
    waitlistTab: (count: number | undefined) =>
      count === undefined ? "Waitlist" : `Waitlist (${count})`,
    tabsLabel: "Attendance",
    rsvped: (relative: string) => `RSVPed ${relative}`,
    joined: (relative: string) => `Joined ${relative}`,
    noAttendeesTitle: "No confirmed attendees yet",
    noAttendeesBody: "People who reserve a seat appear here.",
    noWaitlistTitle: "Nobody's waiting",
    noWaitlistBody: "When the event fills up, the queue appears here in order.",
    dangerTitle: "Danger zone",
    dangerBody:
      "Cancelling tells every confirmed and waitlisted person the event is off. It can't be undone.",
    cancelEvent: "Cancel event",
    notUpcoming: "This event is no longer upcoming, so it can't be changed.",
    errorTitle: "Couldn't load this event",
  },

  cancelModal: {
    title: "Cancel this event?",
    body: (confirmed: number) =>
      `All ${confirmed} ${confirmed === 1 ? "attendee" : "attendees"} will be emailed to let them know this event is no longer happening. Waitlisted members will also be notified. This action cannot be undone.`,
    keep: "Keep event",
    confirm: "Yes, cancel event",
  },
} as const;
