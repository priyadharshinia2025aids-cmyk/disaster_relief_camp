# IntelliJ Project Structure

This project intentionally does not use DTO classes.

Main application layers:

- controller -> Java classes (`public class`)
- service -> Java classes (`public class`)
- model -> Java classes (`public class`)
- repository -> Java interfaces (`public interface` extending JpaRepository)

Repository files are intentionally interfaces so IntelliJ should display them with the green `I` icon when the Project tool window is set to **Packages** (or when IntelliJ is showing Java symbols).

To see the blue class `C` and green interface `I` icons like the reference screenshot:
1. Open the Project tool window.
2. Click the view dropdown at the top (where it may say `Project`).
3. Select `Packages`.
4. Expand `com > reliefcamp > tracker`.

Expected structure:

controller
  CampController
  DashboardController
  DistributionController
  FamilyController
  SupplyController

model
  Camp
  Distribution
  Family
  Supply

repository
  CampRepository       (interface)
  DistributionRepository (interface)
  FamilyRepository    (interface)
  SupplyRepository    (interface)

service
  CampService
  DashboardService
  DistributionService
  FamilyService
  NotificationService
  SupplyService

Support packages `config` and `exception` are also included for web configuration and error handling; they are not DTOs.
