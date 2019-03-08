package ca.mcgill.ecse211.arms;

public class ArmController {
    
    private Claw claw;
    private Elbow elbow;

    public ArmController(Claw claw, Elbow elbow) {
        this.claw = claw;
        this.elbow = elbow;
    }
}
