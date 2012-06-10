// Automatically Generated -- DO NOT EDIT
// org.openintents.cloudsync.shared.CloudSyncRequestFactory
package org.openintents.cloudsync.shared;
import java.util.Arrays;
import com.google.web.bindery.requestfactory.vm.impl.OperationData;
import com.google.web.bindery.requestfactory.vm.impl.OperationKey;
public final class CloudSyncRequestFactoryDeobfuscatorBuilder extends com.google.web.bindery.requestfactory.vm.impl.Deobfuscator.Builder {
{
withOperation(new OperationKey("igmnaNXEMJsve9_Nr9Nvc0b1R1w="),
  new OperationData.Builder()
  .withClientMethodDescriptor("(Lorg/openintents/cloudsync/shared/TaskProxy;)Lcom/google/web/bindery/requestfactory/shared/Request;")
  .withDomainMethodDescriptor("(Lorg/openintents/cloudsync/server/Task;)V")
  .withMethodName("deleteTask")
  .withRequestContext("org.openintents.cloudsync.shared.CloudSyncRequest")
  .build());
withOperation(new OperationKey("1swTVwqX1kfxUlaOO6Z4no4OBbI="),
  new OperationData.Builder()
  .withClientMethodDescriptor("(Ljava/lang/String;Ljava/lang/Long;)Lcom/google/web/bindery/requestfactory/shared/Request;")
  .withDomainMethodDescriptor("(Ljava/lang/String;Ljava/lang/Long;)Ljava/util/List;")
  .withMethodName("queryTasks")
  .withRequestContext("org.openintents.cloudsync.shared.CloudSyncRequest")
  .build());
withOperation(new OperationKey("5gI7lsFf7YoEMAHsHBQBpblARew="),
  new OperationData.Builder()
  .withClientMethodDescriptor("()Lcom/google/web/bindery/requestfactory/shared/Request;")
  .withDomainMethodDescriptor("()Ljava/util/List;")
  .withMethodName("queryTasks")
  .withRequestContext("org.openintents.cloudsync.shared.CloudSyncRequest")
  .build());
withOperation(new OperationKey("YPeD18Im_wwBFezyHLFNW9kLn7g="),
  new OperationData.Builder()
  .withClientMethodDescriptor("(Ljava/lang/String;)Lcom/google/web/bindery/requestfactory/shared/Request;")
  .withDomainMethodDescriptor("(Ljava/lang/String;)Ljava/util/List;")
  .withMethodName("queryTasks")
  .withRequestContext("org.openintents.cloudsync.shared.CloudSyncRequest")
  .build());
withOperation(new OperationKey("7B2S0lDU2ksQ60knvyCp2EAOov4="),
  new OperationData.Builder()
  .withClientMethodDescriptor("(Ljava/lang/String;Ljava/util/List;)Lcom/google/web/bindery/requestfactory/shared/Request;")
  .withDomainMethodDescriptor("(Ljava/lang/String;Ljava/util/List;)Ljava/util/List;")
  .withMethodName("queryGoogleIdList")
  .withRequestContext("org.openintents.cloudsync.shared.CloudSyncRequest")
  .build());
withOperation(new OperationKey("3M_pQpz$Ek3TJj$OpFFadM2UKWg="),
  new OperationData.Builder()
  .withClientMethodDescriptor("()Lcom/google/web/bindery/requestfactory/shared/Request;")
  .withDomainMethodDescriptor("()Lorg/openintents/cloudsync/server/Task;")
  .withMethodName("createTask")
  .withRequestContext("org.openintents.cloudsync.shared.CloudSyncRequest")
  .build());
withOperation(new OperationKey("RVhbl3Gqn_$wCvMzuxH$_n5A3Ig="),
  new OperationData.Builder()
  .withClientMethodDescriptor("(Ljava/lang/String;Ljava/lang/Long;)Lcom/google/web/bindery/requestfactory/shared/Request;")
  .withDomainMethodDescriptor("(Ljava/lang/String;Ljava/lang/Long;)Ljava/util/List;")
  .withMethodName("queryExactTimeStamp")
  .withRequestContext("org.openintents.cloudsync.shared.CloudSyncRequest")
  .build());
withOperation(new OperationKey("Wv2lPXIBvKctKkALV_Mhw2mLlAw="),
  new OperationData.Builder()
  .withClientMethodDescriptor("()Lcom/google/web/bindery/requestfactory/shared/Request;")
  .withDomainMethodDescriptor("()Ljava/lang/Long;")
  .withMethodName("getAppEngineTime")
  .withRequestContext("org.openintents.cloudsync.shared.CloudSyncRequest")
  .build());
withOperation(new OperationKey("hgOjfXaQT6KnVSjzBoleiyZaZ20="),
  new OperationData.Builder()
  .withClientMethodDescriptor("(Ljava/lang/Long;)Lcom/google/web/bindery/requestfactory/shared/Request;")
  .withDomainMethodDescriptor("(Ljava/lang/Long;)Lorg/openintents/cloudsync/server/Task;")
  .withMethodName("readTask")
  .withRequestContext("org.openintents.cloudsync.shared.CloudSyncRequest")
  .build());
withOperation(new OperationKey("o_AAA5PPDuD$vNDAfjeX3pP1EL4="),
  new OperationData.Builder()
  .withClientMethodDescriptor("(Lorg/openintents/cloudsync/shared/TaskProxy;)Lcom/google/web/bindery/requestfactory/shared/Request;")
  .withDomainMethodDescriptor("(Lorg/openintents/cloudsync/server/Task;)Lorg/openintents/cloudsync/server/Task;")
  .withMethodName("updateTask")
  .withRequestContext("org.openintents.cloudsync.shared.CloudSyncRequest")
  .build());
withRawTypeToken("8KVVbwaaAtl6KgQNlOTsLCp9TIU=", "com.google.web.bindery.requestfactory.shared.ValueProxy");
withRawTypeToken("FXHD5YU0TiUl3uBaepdkYaowx9k=", "com.google.web.bindery.requestfactory.shared.BaseProxy");
withRawTypeToken("qOzle54SS57J8llA_Qr8ydDwRdA=", "org.openintents.cloudsync.shared.TaskProxy");
withClientToDomainMappings("org.openintents.cloudsync.server.Task", Arrays.asList("org.openintents.cloudsync.shared.TaskProxy"));
}}
