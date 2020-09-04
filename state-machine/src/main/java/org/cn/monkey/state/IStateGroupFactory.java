package org.cn.monkey.state;

public interface IStateGroupFactory {

    IStateGroup find(long roomId);

    IStateGroup create(ICharacter creator, IStateGroupConfig config);

}
