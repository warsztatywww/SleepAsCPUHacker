package pl.krzysh.sleepasandroid.captcha.cpuhacker;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Random;

public class CPUHackerChallenge implements Parcelable {
    public String instruction;
    public int[] inputs = new int[REGISTERS.length];
    public int[] outputs = new int[REGISTERS.length];

    public static final String[] OPCODES = new String[] { "MOV", "XCHG", "ADD", "SUB", "INC", "DEC" };
    public static final String[] REGISTERS = new String[] { "EAX", "EBX", "ECX", "EDX" };

    private CPUHackerChallenge() {

    }

    public static CPUHackerChallenge generate() {
        Random random = new Random();
        CPUHackerChallenge challenge = new CPUHackerChallenge();

        for(int i = 0; i < REGISTERS.length; ++i)
            challenge.inputs[i] = random.nextInt();
        for(int i = 0; i < REGISTERS.length; ++i)
            challenge.outputs[i] = challenge.inputs[i];

        int reg1 = random.nextInt(REGISTERS.length);
        int reg2 = random.nextInt(REGISTERS.length);
        switch(OPCODES[random.nextInt(OPCODES.length)]) {
            case "MOV":
                challenge.instruction = "MOV " + REGISTERS[reg1] + ", " + REGISTERS[reg2];
                challenge.outputs[reg1] = challenge.inputs[reg2];
                break;
            case "XCHG":
                challenge.instruction = "XCHG " + REGISTERS[reg1] + ", " + REGISTERS[reg2];
                challenge.outputs[reg1] = challenge.inputs[reg2];
                challenge.outputs[reg2] = challenge.inputs[reg1];
                break;
            case "ADD":
                challenge.instruction = "ADD " + REGISTERS[reg1] + ", " + REGISTERS[reg2];
                challenge.outputs[reg1] = challenge.inputs[reg1] + challenge.inputs[reg2];
                break;
            case "SUB":
                challenge.instruction = "SUB " + REGISTERS[reg1] + ", " + REGISTERS[reg2];
                challenge.outputs[reg1] = challenge.inputs[reg1] - challenge.inputs[reg2];
                break;
            case "INC":
                challenge.instruction = "INC " + REGISTERS[reg1];
                challenge.outputs[reg1] = challenge.inputs[reg1] + 1;
                break;
            case "DEC":
                challenge.instruction = "DEC " + REGISTERS[reg1];
                challenge.outputs[reg1] = challenge.inputs[reg1] - 1;
                break;
        }

        return challenge;
    }


    public CPUHackerChallenge(Parcel in) {
        instruction = in.readString();
        for(int i = 0; i < REGISTERS.length; ++i)
            inputs[i] = in.readInt();
        for(int i = 0; i < REGISTERS.length; ++i)
            outputs[i] = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(instruction);
        for(int i = 0; i < REGISTERS.length; ++i)
            dest.writeInt(inputs[i]);
        for(int i = 0; i < REGISTERS.length; ++i)
            dest.writeInt(outputs[i]);
    }

    public static final Parcelable.Creator<CPUHackerChallenge> CREATOR
        = new Parcelable.Creator<CPUHackerChallenge>() {
        public CPUHackerChallenge createFromParcel(Parcel in) {
            return new CPUHackerChallenge(in);
        }

        public CPUHackerChallenge[] newArray(int size) {
            return new CPUHackerChallenge[size];
        }
    };
}
