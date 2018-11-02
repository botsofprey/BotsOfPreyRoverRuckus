Actions are any objects or things that are meant to be activated or run during the autonomous or user controller.

These can include ball shooters, propellors, arms, etc.

This year, our actions include the GlyphPicker, GlyphExtender, RAD, JewelJouster, and the Flag Controller

These are meant to be stored in objects that implement the ActionHandler interface and have distinct functions to call

These operations can be threaded or instantaneous.

IMPORTANT:
    Any Actions must implement the stop() function which de-initializes any motors, servos, or sensors the Action uses.
