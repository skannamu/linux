package com.skannamu.init; //패키지

import com.skannamu.item.block.standardBlock;
//standardBlock클래스(실제 블럭의 특성 & 기능구현한 클래스, 객체가 됨)
import com.skannamu.skannamuMod;
//skannamuMod의 MOD_ID, Log사용을 위해 임포트
import net.minecraft.block.Block;
//모든 블럭의 기본이 되는 클래스. STANDARD_BLOCK의 변수타입
//registeredBlock의 변수타입 & 메서드들의 반환타입
import net.minecraft.item.BlockItem;
//블럭을 설치가능하게 만드는 클래스. 블럭을 손에 들고있게하거나/
//이 아이템이 사용되면 설치되게.
import net.minecraft.item.Item;
//모든 아이템의 기본이 되는 클래스
//RegisteryKey<Item>타입 파라미터로 사용
import net.minecraft.registry.Registry;
//실제로 마인크래프트에 블럭과 아이템을 등록
import net.minecraft.registry.RegistryKey;
//레지스트리 키 클래스. 템을 고유하게 식별하는 키.
import net.minecraft.registry.RegistryKeys;
//키값을 생성하기 위한 재료정도로 생각하면 될듯
import net.minecraft.registry.Registries;
//블럭 또는 아이템이 등록되는 실제 레지스트리. ex> Registries.BLOCK은 마인크래프트의 모든 블럭객체가 등록되는 실제 레지스트리 인스턴스
import net.minecraft.util.Identifier;
//블럭 아이템등 마크의 모든 리소스를 모드ID:이름 형태로 매핑해서 인식하는것
public class BlockInitialization {

    public static Block STANDARD_BLOCK;

    private static Block registerBlockWithItem(String name) {  // 수정: Block 매개변수 제거, name만 받음
        Identifier id = Identifier.of(skannamuMod.MOD_ID, name);
        //id는 인식자임. skannamuMod.MOD_ID와 아이템 이름을 매핑한다. 이게 id가 됨.
        RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, id);
                                        //RegistryKeys.BLOCK는 상수. id도 상수. 즉
                                        //RegistryKey.of(~~)는 뒤의 두개를 조합해서 키를 생성함.
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, id);
                                            //이것도 마찬가지
            //itemKey는 Item형 값들(맵) = ...이렇게 만들어진다~~
        // Settings 생성 및 key 설정
        Block.Settings blockSettings = Block.Settings.create()
                //Settings는 Block안에 있는 메서드. 즉 blockSettings는
                //Block타입이 아니라 Block.Settings타입임.
                //Block.Settings.create()는 세팅설정을 시작함
                .registryKey(blockKey)
                //그 블럭의 고유 키 설정
                .strength(1.5f) // 파괴강도
                .requiresTool();//적절한 도구를 썼는가

        Block registeredBlock = Registry.register(Registries.BLOCK, blockKey, new standardBlock(blockSettings));
        //Registry.register = 등록하겠다          //마크모든블록있는레지스트리         //새로운 standardBlock인스턴스생성
        Item.Settings itemSettings = new Item.Settings()
                //Item.Settings형 itemSettings 변수선언하고 계속 설정
                .registryKey(itemKey)//...registryKey(itemKey는 아이템이름과 아이디가있음) 이걸 등록함
                .useBlockPrefixedTranslationKey();
                //아이템의 번역키?..?

        Registry.register(//등록하겠다 선언
                Registries.ITEM,//아이템 레지스트리에다가
                itemKey, //아이템 키랑(itemKey는 아이템 키 상수랑 id있는 맵, RegistryKey형)
                new BlockItem(registeredBlock, itemSettings)
                //설치되는 블럭을 등록. registeredBlock은 키값하고 다 등록됨. 블럭과 itemSetting값으로 등록)

        );

        return registeredBlock;
        //마지막으로 블럭을 반환하는데, 일단 모든 블럭이 있는 레지스트리에다가 블럭키랑 우리가 구현한 기능의 블럭을
        //저장한 registerdBlock을 반환함 즉 registerdBlock은 블럭키와 우리가 구현한 블럭이 등록된 상태의 블럭임.
    }

    public static void initializeBlocks() {
        STANDARD_BLOCK = registerBlockWithItem("standard_block");
        //
        skannamuMod.LOGGER.info("Registered Standard Block successfully!");
    }
}