MYRA AI ASSISTANT — AMIT EDITION

FUTURISTIC RGB EDGE LIGHT + HINDI VOICE + AI PHONE ASSISTANT

Create a complete native Android application named:

MYRA AI Assistant

Personalized user name:

AMIT

The assistant must always address the user naturally as:

"अमित"

Default conversation language:

HINDI

The application must be a real native Android application using:

Kotlin
Jetpack Compose
Material 3

Do NOT create a website or PWA.

==================================================

1. MYRA PERSONALITY
   ==================================================

MYRA is a friendly futuristic Hindi AI assistant.

When the application starts, MYRA should say:

"नमस्ते अमित! मैं MYRA हूँ। बताइए, मैं आपकी क्या मदद करूँ?"

Examples:

"जी अमित।"
"अमित, मैं आपकी मदद कर सकती हूँ।"
"जी अमित, command बताइए।"
"अमित, यह काम पूरा हो गया है।"

Default response language:

Hindi.

Understand:

Hindi
Hinglish
English

Examples:

"YouTube kholo"
"YouTube खोलो"
"Open YouTube"

All should be understood.

==================================================
2. FUTURISTIC RGB RUNNING EDGE LIGHT

Create a premium RGB LED-style visual effect around the ENTIRE edge of the application screen.

The effect must look like a futuristic RGB running light.

The light should travel continuously around all four sides:

TOP
RIGHT
BOTTOM
LEFT

Animation direction:

Clockwise.

Visual example:

TOP:
→ → → → →

RIGHT:
↓
↓
↓

BOTTOM:
← ← ← ← ←

LEFT:
↑
↑
↑

The RGB light must continuously move around the screen edges.

==================================================
3. RGB COLOR SYSTEM

Use a smooth continuously changing RGB spectrum.

Colors should include:

Red
Orange
Yellow
Green
Cyan
Blue
Purple
Magenta
Pink

Do not abruptly switch colors.

Use smooth color interpolation.

The edge light should have:

Glow
Blur
Soft neon effect
Moving highlight
Gradient trail

The running light should look like a premium gaming RGB setup.

==================================================
4. FOUR-SIDE RGB EFFECT

The RGB animation must cover all four screen edges.

Use a Compose Canvas or another efficient native rendering method.

Do NOT use a huge number of individual UI elements.

Optimize the animation for mobile performance.

The effect should remain smooth on mid-range Android phones.

Target:

60 FPS when possible.

Avoid unnecessary battery consumption.

==================================================
5. BACKGROUND RGB GLOW

Add a very subtle RGB ambient glow in the background.

The background must remain dark.

Use:

Black / very dark background

with extremely soft:

Red
Blue
Purple
Cyan

RGB ambient glow.

The background glow should slowly change color.

Do NOT make the background too bright.

Text and controls must remain clearly readable.

==================================================
6. RGB INTENSITY

Default RGB edge brightness:

Medium.

Default glow:

Low.

Add Settings controls:

RGB Edge Light:
ON / OFF

RGB Brightness:
Low
Medium
High

RGB Speed:
Slow
Normal
Fast

Background Glow:
ON / OFF

This allows Amit to customize the effect.

==================================================
7. APP STARTUP

When MYRA opens:

First show a short futuristic startup animation.

Display:

MYRA

AI ASSISTANT

Then:

"नमस्ते अमित"

Then:

"मैं आपकी मदद के लिए तैयार हूँ।"

The RGB edge animation should start immediately when the MYRA interface becomes visible.

==================================================
8. IMPORTANT 24-HOUR REQUIREMENT

The user wants MYRA's visual RGB system to feel continuously active.

Implement the RGB animation continuously while the MYRA application UI is active/visible.

IMPORTANT:

Do NOT secretly keep the screen awake for 24 hours.

Do NOT secretly run a hidden background process.

Do NOT bypass Android battery restrictions.

Provide a setting:

"24 घंटे Visual Mode"

When enabled:

MYRA keeps the RGB visual effect active whenever MYRA is running and visible.

Show information:

"Android background restrictions के कारण MYRA बंद होने पर display animation लगातार चलाना संभव नहीं हो सकता।"

If the user wants an always-visible RGB effect outside MYRA, explain that Android does not allow a normal app to draw continuously over the entire system UI without appropriate user-granted overlay functionality.

Do not bypass Android restrictions.

==================================================
9. MAIN HOME SCREEN

Create a premium futuristic dashboard.

Layout:

Top:

MYRA
AI ASSISTANT

Under it:

"नमस्ते अमित"

Center:

Large futuristic AI core / microphone.

Microphone button should have:

Soft glow
Pulse animation
RGB reflection

Text:

"MYRA से कुछ भी पूछें..."

Bottom:

Home
Chat
Actions
Settings

The RGB edge light must remain visible around this entire screen.

==================================================
10. VOICE INPUT

When Amit taps the microphone:

Show:

"सुन रही हूँ अमित..."

Start Android SpeechRecognizer.

Support Hindi and English.

Show recognized text.

Example:

Amit:

"MYRA YouTube खोलो"

MYRA:

"जी अमित, YouTube खोल रही हूँ।"

==================================================
11. MYRA VOICE OUTPUT

Use Android TextToSpeech.

Default:

Hindi voice.

MYRA should speak naturally.

Examples:

"जी अमित।"
"अमित, YouTube खोल रही हूँ।"
"अमित, यह application आपके फोन में नहीं मिली।"
"अमित, Accessibility permission अभी बंद है।"

Add Settings:

Voice language
Voice speed
Voice pitch
Speak responses ON/OFF

==================================================
12. APP LAUNCHING

Implement real Android app launching.

Commands:

"YouTube खोलो"
"WhatsApp खोलो"
"Instagram खोलो"
"Chrome खोलो"
"Camera खोलो"
"Gallery खोलो"
"Settings खोलो"
"Maps खोलो"

Resolve installed applications dynamically.

Do not hardcode only one phone model.

==================================================
13. ACCESSIBILITY SERVICE

Create:

MyraAccessibilityService.kt

NodeFinder.kt

ScreenReader.kt

GestureController.kt

Support, where Android allows:

Back
Home
Scroll
Swipe
Tap
Click accessible buttons
Read accessible text
Find visible text
Type into supported fields
Inspect accessibility nodes

Use proper Android AccessibilityService APIs.

The user must manually enable MYRA through Android Settings.

Never enable Accessibility secretly.

==================================================
14. ACCESSIBILITY COMMANDS

Support:

"Back जाओ"

"Home जाओ"

"ऊपर scroll करो"

"नीचे scroll करो"

"Search पर click करो"

"इस button को दबाओ"

"यहाँ Amit Kumar लिखो"

The system should first attempt to locate accessible UI elements.

Do not claim success when an element cannot be found.

==================================================
15. PHONE CONTROL

Support safe Android actions:

Flashlight
Volume
Settings
Wi-Fi settings
Bluetooth settings
Display settings
Battery settings

Use official Android APIs.

Do not bypass Android restrictions.

==================================================
16. CALLING

Support:

"मम्मी को call करो"

"भाई को call करो"

"अमित को call करो"

Always confirm before making a call:

"अमित, क्या मैं इस contact को call करूँ?"

Buttons:

हाँ
नहीं

Never make silent calls.

==================================================
17. NOTIFICATIONS

Implement NotificationListenerService.

Allow Amit to enable Notification Access manually.

Command:

"MYRA मेरी notifications पढ़ो।"

MYRA should only read notifications accessible through the granted service.

Explain privacy clearly.

==================================================
18. AI CHAT

Create a Chat screen.

MYRA should answer general questions in Hindi by default.

UI:

User message
MYRA response
Typing animation
Timestamp

Use an AIClient abstraction.

Do not hardcode secret API keys into the APK.

==================================================
19. AI ACTION PLANNER

Support multi-step commands.

Example:

"MYRA YouTube खोलकर Amit Kumar search करो"

Planner:

1. Open YouTube
2. Wait for app
3. Find Search
4. Click Search
5. Type Amit Kumar
6. Submit search

Validate each action.

If an action fails:

"अमित, मैं इस step को पूरा नहीं कर पाई।"

Never fake success.

==================================================
20. SETTINGS

Create Settings screen.

Sections:

MYRA

VOICE

ACCESSIBILITY

NOTIFICATIONS

AI

PRIVACY

RGB LIGHT

ABOUT

RGB settings:

RGB Light:
ON/OFF

RGB Speed:
Slow
Normal
Fast

RGB Brightness:
Low
Medium
High

Background RGB Glow:
ON/OFF

24 Hour Visual Mode:
ON/OFF

IMPORTANT:

"24 Hour Visual Mode" must never secretly bypass Android background or battery restrictions.

==================================================
21. RGB ANIMATION TECHNICAL REQUIREMENTS

Implement RGB edge animation efficiently using Jetpack Compose Canvas or another efficient native Android rendering mechanism.

Use:

infiniteTransition

Animatable

Canvas

Brush

LinearGradient / SweepGradient

Blur / glow effects where performance permits

Create a reusable component:

RgbEdgeLight()

Example architecture:

ui/components/RgbEdgeLight.kt

Parameters:

enabled
speed
brightness
backgroundGlow
colorMode

The component must draw a moving RGB highlight around all four screen edges.

Use smooth hue interpolation.

The RGB light should appear as:

Neon
Soft
Premium
Futuristic

==================================================
22. RGB LIGHT STATES

Normal state:

Slow RGB running light.

Listening state:

Increase brightness slightly.

Processing state:

Use faster RGB animation.

Success state:

Brief green highlight.

Error state:

Brief red highlight.

Idle state:

Slow smooth RGB cycle.

Do not make the effects distracting.

==================================================
23. PERFORMANCE

Optimize for Redmi 10 Prime and similar mid-range devices.

Avoid:

Memory leaks
Infinite uncontrolled coroutines
Unnecessary background services
High CPU usage
High battery usage

Stop animation resources when the UI is destroyed.

Use lifecycle-aware components.

==================================================
24. PRIVACY

Never:

Read passwords secretly
Read OTPs secretly
Perform banking transactions
Send messages without proper confirmation
Make calls without confirmation
Enable Accessibility automatically
Enable Notification Access automatically
Record microphone continuously without user knowledge
Bypass Android security
Create hidden surveillance

All sensitive permissions must be user-controlled.

==================================================
25. PROJECT STRUCTURE

Create:

com.myra.aiassistant

ui/
HomeScreen.kt
ChatScreen.kt
ActionsScreen.kt
SettingsScreen.kt
PermissionScreen.kt

ui/components/
RgbEdgeLight.kt
MyraMicButton.kt
MyraTopBar.kt
MyraBottomNavigation.kt

voice/
SpeechRecognizerManager.kt
TextToSpeechManager.kt
VoiceCommandProcessor.kt

commands/
CommandParser.kt
IntentType.kt
CommandPlanner.kt
ActionExecutor.kt

accessibility/
MyraAccessibilityService.kt
NodeFinder.kt
ScreenReader.kt
GestureController.kt

services/
MyraNotificationListener.kt

apps/
AppLauncher.kt
AppResolver.kt

phone/
CallManager.kt
ContactManager.kt

ai/
AIClient.kt
PromptBuilder.kt
AIActionPlanner.kt

data/
MyraPreferences.kt
CommandHistory.kt

utils/
PermissionUtils.kt
DeviceUtils.kt

==================================================
26. STARTUP EXPERIENCE

Startup sequence:

Dark screen

RGB edge light begins.

Center:

MYRA

AI ASSISTANT

Then:

"नमस्ते अमित ❤️"

Then:

"मैं MYRA हूँ।"

Then:

"बताइए अमित, आज मैं आपकी क्या मदद करूँ?"

Then show microphone.

RGB animation continues.

==================================================
27. FINAL BUILD REQUIREMENTS

Generate a complete native Kotlin Android project.

Use:

Kotlin
Jetpack Compose
Material 3

Make the project compile.

Fix imports.

Fix dependencies.

Fix AndroidManifest.

Fix accessibility configuration.

Fix Compose errors.

Fix lifecycle issues.

Fix voice recognition lifecycle.

Fix TextToSpeech lifecycle.

Fix RGB animation lifecycle.

Do not create fake functionality.

Do not leave core features as TODO.

If Android prevents a feature, implement the closest supported API and clearly explain the limitation.

The final application should feel like:

MYRA AI ASSISTANT — AMIT EDITION

with:

Hindi voice
Personalized Amit greeting
Futuristic dark UI
Four-side RGB running light
RGB background glow
Animated microphone
Voice commands
App launching
Accessibility controls
Notifications
Calling with confirmation
AI chat
AI action planning
Privacy controls
RGB customization

The RGB edge light should be one of the main visual identities of MYRA.

Make it premium, smooth, futuristic and optimized for Android.
