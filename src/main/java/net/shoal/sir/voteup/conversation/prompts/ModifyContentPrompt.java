package net.shoal.sir.voteup.conversation.prompts;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.config.SoundManager;
import net.shoal.sir.voteup.config.VoteManager;
import net.shoal.sir.voteup.data.Vote;
import net.shoal.sir.voteup.enums.MessageType;
import net.shoal.sir.voteup.enums.VoteDataType;
import net.shoal.sir.voteup.util.ChatAPIUtil;
import net.shoal.sir.voteup.util.CommonUtil;
import net.shoal.sir.voteup.util.LocaleUtil;
import net.shoal.sir.voteup.util.PlaceholderUtil;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.ValidatingPrompt;
import org.bukkit.entity.Player;

import java.util.List;

public class ModifyContentPrompt extends ValidatingPrompt {

    private LocaleUtil locale;

    private Player user;
    private String voteID;
    private VoteDataType type;
    private int index;
    private String actionType;

    public ModifyContentPrompt(Player player, VoteDataType type, int index, String actionType) {
        this.user = player;
        this.voteID = player.getName();
        this.type = type;
        this.index = index;
        this.actionType = actionType;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        locale = VoteUp.getInstance().getLocale();
        return locale.buildMessage(VoteUp.LOCALE, MessageType.INFO, "&7请输入您想要修改/添加的内容, 当前修改内容类型: &c" + type.getName());
    }

    @Override
    protected boolean isInputValid(ConversationContext context, String input) {
        return type == VoteDataType.DESCRIPTION || type == VoteDataType.AUTOCAST;
    }

    @Override
    protected Prompt acceptValidatedInput(ConversationContext context, String input) {
        locale = VoteUp.getInstance().getLocale();
        locale.debug("&7(ModifyContentPrompt) 会话输入值已验证通过.");
        locale.debug("&7目标数据类型: &c" + type.getName());
        locale.debug("&7新内容(输入值): &c" + input);
        Vote creating = VoteManager.getInstance().getCreatingVote(voteID);

        List<String> list;
        if(type == VoteDataType.DESCRIPTION) {
            list = creating.getDescription();
            input = CommonUtil.color(input);
        } else {
            list = creating.getAutoCast();
        }

        switch (actionType) {
            case "add":
                if(index > list.size()) {
                    list.add(input);
                } else {
                    list.add(index, input);
                }
                break;
            case "set":
                if(index > list.size()) {
                    list.set(list.size() - 1, input);
                } else {
                    list.set(index, input);
                }
                break;
            default:
                return Prompt.END_OF_CONVERSATION;
        }

        boolean result = VoteManager.getInstance().setCreatingVoteData(voteID, type, list);
        locale.debug("&7设置值: &c" + (result ? "成功" : "失败"));

        SoundManager.getInstance().ding(user.getName());
        if(type == VoteDataType.DESCRIPTION) {
            ChatAPIUtil.sendEditableList(
                    user,
                    creating.getDescription(),
                    PlaceholderUtil.check(CommonUtil.color("&7投票 &c%TITLE% &7的简述信息 &6&l>>>"), creating),
                    "&a&l[Add] ",
                    "/vote modify desc add ",
                    "&e&l[Edit] ",
                    "/vote modify desc set",
                    "&c&l[Del] ",
                    "/vote modify desc del ",
                    "&a&l>>> &7返回编辑菜单",
                    "/vote create back"
            );
        } else {
            ChatAPIUtil.sendEditableList(
                    user,
                    creating.getAutoCast(),
                    PlaceholderUtil.check(CommonUtil.color("&7投票 &c%TITLE% &7的自动执行命令列表 &6&l>>>"), creating),
                    "&a&l[Add] ",
                    "/vote modify autocast add ",
                    "&e&l[Edit] ",
                    "/vote modify autocast set",
                    "&c&l[Del] ",
                    "/vote modify autocast del ",
                    "&a&l>>> &7返回编辑菜单",
                    "/vote create back"
            );
        }
        return Prompt.END_OF_CONVERSATION;
    }
}
