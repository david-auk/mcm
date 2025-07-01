export default interface ServerInstance {
  id: string,
  name: string,
  description?: string,
  minecraftVersion: string,
  jarUrl: string,
  eulaAccepted: boolean, // “initialized”
  createdAt: string,
  allocatedRamMB: number,
  port: number,
  running: boolean
}