package org.cn.monkey.state;

import com.google.common.base.Preconditions;
import org.cn.monkey.cmd.proto.Command;

public interface IStateGroup {

    class CharacterCmdPair {
        private final ICharacter character;

        private final Command.Cmd cmd;

        public CharacterCmdPair(ICharacter character, Command.Cmd cmd) {
            Preconditions.checkNotNull(character);
            Preconditions.checkNotNull(cmd);
            this.character = character;
            this.cmd = cmd;
        }

        public Command.Cmd getCmd() {
            return this.cmd;
        }

        public ICharacter getCharacter() {
            return this.character;
        }
    }

    long getId();

    IStateData getStateData();

    void addState(IState state);

    void setCurrentState(String stateCode);

    boolean enter(ICharacter character);

    boolean isFullCharacter();

    boolean addCharacterCmd(ICharacter character, Command.Cmd cmd);

    void update();

    boolean canDepose();
}
