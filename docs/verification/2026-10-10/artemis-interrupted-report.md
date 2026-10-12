# LingUBible Exploration Report

## Baseline Device & Environment
- Device resolution: 480x1000
- Density: 180
- Orientation / user_rotation: 0 (portrait)
- accelerometer_rotation: 0
- font_scale: 1.0
- Baseline Visual Appearance: Red accent / Dark / OLED black background

## Startup / Home Data
- Home Statistics Cards:
  - 認證學生 (Verified Students): 585 (+32)
  - 課程評價 (Course Reviews): 1323 (+42)
  - 涵蓋課程 (Covered Courses): 1083 (+8)
  - 評價講師 (Reviewed Instructors): 791 (+5)
- Navigation bar items:
  - 探索 (Course Discovery)
  - 學業工具 (Academic Tools)
  - 用戶設定 (User Settings)
- Header items:
  - App Logo & title: LingUBible
  - Search button: present
  - User avatar: present

## Course Search & Discovery Inspection (Goal 2 Findings)
- Attempted course search for BUS1102 and browsed default category lists ("全部 ALL", "商學 BUS").
- Observed UI State: Screen shows "共 0 門課程", "找不到相關課程", and persistent loading indicator "正在載入課程...".
- System Logcat Diagnostics:
  - `E CourseRepo: Error fetching courses: Invalid query method:`
  - `U2.a: Invalid query method:`
  - `CourseRepo: getCourses called: search=BUS1102, subject=null`
  - Thrown across all course pagination requests (page 0, 1, 2, ...).
- Evaluation: Optimization/R8 bytecode obfuscation issue in releaseValidation build stripping/altering query method in repository.
- As instructed, stopped this branch without claiming course tabs passed; documented diagnostic evidence.


## Auth & Registration Inspection (Goal 3 Findings)
- Inspected "登入 Sign In" and "註冊 Register" segmented tab transition.
- Register form contains:
  - 姓名或暱稱 · Name
  - 嶺南大學電郵 · Lingnan Email
  - 密碼 · Password
  - 確認密碼 · Confirm Password
- Tested software keyboard interaction:
  - Tapped Name field to focus and open soft keyboard.
  - Pressed system BACK key.
  - Verified: Soft keyboard was dismissed cleanly while remaining on the registration form (assertion passed).

