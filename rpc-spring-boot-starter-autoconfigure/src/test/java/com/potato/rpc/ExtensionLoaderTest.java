package com.potato.rpc;

import com.potato.rpc.common.extension.ExtensionLoader;
import com.potato.rpc.register.ServiceRegistry;
import com.potato.rpc.serializer.PotatoSerializer;
import org.junit.jupiter.api.Test;

/**
 * ExtensionLoader
 *
 * @author lizhifu
 * @date 2021/7/13
 */
public class ExtensionLoaderTest {
    @Test
    public void test(){
        ExtensionLoader.getExtensionLoader(PotatoSerializer.class).getExtension("jdk");
        ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension("nacos");

    }
}
